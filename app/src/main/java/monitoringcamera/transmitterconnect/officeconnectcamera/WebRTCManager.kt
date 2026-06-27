package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import org.webrtc.*
import java.io.File
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

class WebRTCManager(private val context: Context) {
    private val TAG = "WebRTCManager"
    private val STUN_SERVER = "stun:stun.l.google.com:19302"

    private val database = FirebaseDatabase.getInstance()
    
    // Shared Media Resources
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localMediaStream: MediaStream? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    // Session Management
    private val hostSessions = ConcurrentHashMap<String, HostSessionInfo>()
    private val viewerSessions = ConcurrentHashMap<String, ViewerSessionInfo>()

    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionEvent: ((sessionId: String, isHost: Boolean, remoteDeviceId: String, connected: Boolean) -> Unit)? = null

    // Observable states
    private val _connectionState = MutableLiveData(false)
    val connectionState: LiveData<Boolean> get() = _connectionState

    private val sessionStatuses = ConcurrentHashMap<String, MutableLiveData<String?>>()
    private val sessionConnectionStates = ConcurrentHashMap<String, MutableLiveData<Boolean>>()

    private val _isFrontFacing = MutableLiveData(true)
    val isFrontFacingLiveData: LiveData<Boolean> get() = _isFrontFacing
    private var isFrontFacing = true

    private val _sessionStatus = MutableLiveData<String?>() // Legacy Overall Status
    val sessionStatus: LiveData<String?> get() = _sessionStatus

    private val _isRecording = MutableLiveData(false)
    val isRecordingLiveData: LiveData<Boolean> get() = _isRecording
    private var lastRecordedFile: File? = null

    private var localRenderer: SurfaceViewRenderer? = null
    
    val isBroadcaster: Boolean get() = hostSessions.isNotEmpty()

    @Volatile
    private var isReleased = false
    private val mediaLock = Any()

    companion object {
        private var staticFactory: PeerConnectionFactory? = null
        private var staticEglBase: EglBase? = null

        @Synchronized
        fun initSharedResources(context: Context) {
            if (staticEglBase == null) {
                staticEglBase = EglBase.create()
            }
            if (staticFactory == null) {
                val options = PeerConnectionFactory.InitializationOptions.builder(context)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
                PeerConnectionFactory.initialize(options)

                val videoEncoderFactory = DefaultVideoEncoderFactory(staticEglBase!!.eglBaseContext, true, true)
                val videoDecoderFactory = DefaultVideoDecoderFactory(staticEglBase!!.eglBaseContext)

                staticFactory = PeerConnectionFactory.builder()
                    .setVideoEncoderFactory(videoEncoderFactory)
                    .setVideoDecoderFactory(videoDecoderFactory)
                    .createPeerConnectionFactory()
            }
        }
    }

    init {
        initSharedResources(context)
    }

    fun getEglBaseContext(): EglBase.Context {
        return staticEglBase!!.eglBaseContext
    }

    fun getSessionStatus(sessionId: String): LiveData<String?> {
        return sessionStatuses.getOrPut(sessionId) { MutableLiveData<String?>(null) }
    }

    fun getSessionConnectionState(sessionId: String): LiveData<Boolean> {
        return sessionConnectionStates.getOrPut(sessionId) { MutableLiveData<Boolean>(false) }
    }

    private fun buildRtcConfig(): PeerConnection.RTCConfiguration {
        val iceServers = listOf(
            PeerConnection.IceServer.builder(STUN_SERVER).createIceServer(),
            PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                .setUsername("openrelayproject")
                .setPassword("openrelayproject")
                .createIceServer(),
            PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
                .setUsername("openrelayproject")
                .setPassword("openrelayproject")
                .createIceServer()
        )
        return PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.PLAN_B
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }

    // --- Host Session Class ---
    private inner class HostSessionInfo(val sessionId: String, val deviceId: String, val deviceName: String) {
        val viewerPeerConnections = ConcurrentHashMap<String, PeerConnection>()
        val viewerDataChannels = ConcurrentHashMap<String, DataChannel>()
        val viewerPendingCandidates = ConcurrentHashMap<String, MutableList<IceCandidate>>()
        val viewerAnswerListeners = ConcurrentHashMap<String, ValueEventListener>()
        val viewerCandidatesListeners = ConcurrentHashMap<String, ChildEventListener>()
        var viewersRef: DatabaseReference? = null
        var viewersListener: ValueEventListener? = null
        var sessionRef: DatabaseReference? = null
        var sessionListener: ValueEventListener? = null

        fun stop() {
            viewersListener?.let { viewersRef?.removeEventListener(it) }
            sessionListener?.let { sessionRef?.removeEventListener(it) }
            
            viewerPeerConnections.forEach { (vId, pc) ->
                val answerRef = database.getReference("sessions/$sessionId/viewers/$vId/answer")
                viewerAnswerListeners.remove(vId)?.let { answerRef.removeEventListener(it) }
                
                val candidatesRef = database.getReference("sessions/$sessionId/viewers/$vId/calleeCandidates")
                viewerCandidatesListeners.remove(vId)?.let { candidatesRef.removeEventListener(it) }
                
                try {
                    pc.close()
                    pc.dispose()
                } catch (e: Exception) {}
            }
            viewerPeerConnections.clear()
            viewerPendingCandidates.clear()
            
            val ref = database.getReference("sessions/$sessionId")
            try { ref.onDisconnect().cancel() } catch (e: Exception) {}
            ref.removeValue()
            
            val monitorRef = database.getReference("monitors/$deviceId")
            try { monitorRef.onDisconnect().cancel() } catch (e: Exception) {}
            monitorRef.removeValue()

            val saveSessionRef = database.getReference("SaveSessions/$deviceId/$sessionId")
            try { saveSessionRef.onDisconnect().cancel() } catch (e: Exception) {}
            saveSessionRef.removeValue()
        }
    }

    // --- Viewer Session Class ---
    private inner class ViewerSessionInfo(val sessionId: String, val viewerDeviceId: String, var remoteVideoSink: VideoSink?) {
        var peerConnection: PeerConnection? = null
        var dataChannel: DataChannel? = null
        var remoteVideoTrack: VideoTrack? = null
        val pendingCandidates = mutableListOf<IceCandidate>()
        
        var offerRef: DatabaseReference? = null
        var offerListener: ValueEventListener? = null
        var candidatesRef: DatabaseReference? = null
        var candidatesListener: ChildEventListener? = null
        var sessionRef: DatabaseReference? = null
        var sessionListener: ValueEventListener? = null

        fun stop() {
            offerListener?.let { offerRef?.removeEventListener(it) }
            candidatesListener?.let { candidatesRef?.removeEventListener(it) }
            sessionListener?.let { sessionRef?.removeEventListener(it) }
            
            remoteVideoTrack?.let { track ->
                remoteVideoSink?.let { sink -> track.removeSink(sink) }
            }

            try {
                peerConnection?.close()
                peerConnection?.dispose()
            } catch (e: Exception) {}
            
            val ref = database.getReference("sessions/$sessionId")
            try {
                ref.child("viewers").child(viewerDeviceId).child("answer").onDisconnect().cancel()
                ref.child("viewers").child(viewerDeviceId).child("calleeCandidates").onDisconnect().cancel()
                ref.child("viewers").child(viewerDeviceId).child("status").onDisconnect().cancel()
                
                ref.child("viewers").child(viewerDeviceId).child("answer").removeValue()
                ref.child("viewers").child(viewerDeviceId).child("calleeCandidates").removeValue()
                ref.child("viewers").child(viewerDeviceId).child("status").setValue("Offline")
            } catch (e: Exception) {}

            ref.child("metadata/deviceId").get().addOnSuccessListener { snapshot ->
                val monitorId = snapshot.getValue(String::class.java)
                if (monitorId != null) {
                    database.getReference("monitors/$monitorId/isOccupied").setValue(false)
                }
            }
        }
    }

    fun startCameraAndOffer(
        sessionId: String,
        deviceId: String,
        localSink: VideoSink?,
        isAudioOnly: Boolean,
        deviceName: String,
        callback: OfferCallback
    ) {
        if (hostSessions.containsKey(sessionId)) {
            localSink?.let { localVideoTrack?.addSink(it) }
            return
        }

        val session = HostSessionInfo(sessionId, deviceId, deviceName)
        hostSessions[sessionId] = session
        
        // 1. Ensure Media is started BEFORE setting up listeners to avoid race conditions
        startMedia(localSink, isAudioOnly)
        listenForSessionStatus(session)

        val metadata = mapOf(
            "name" to deviceName,
            "deviceId" to deviceId,
            "sessionId" to sessionId,
            "model" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "status" to "online",
            "type" to "Camera",
            "timestamp" to ServerValue.TIMESTAMP
        )
        database.getReference("sessions/$sessionId/metadata").setValue(metadata)
        database.getReference("sessions/$sessionId").onDisconnect().removeValue()

        val registryRef = database.getReference("monitors/$deviceId")
        registryRef.child("activeSessionId").setValue(sessionId)
        registryRef.child("lastSeen").setValue(ServerValue.TIMESTAMP)
        registryRef.onDisconnect().removeValue()

        session.viewersRef = database.getReference("sessions/$sessionId/viewers")
        session.viewersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val viewerId = child.key ?: continue
                    val status = child.child("status").getValue(String::class.java)
                    if (status == "Online" && !session.viewerPeerConnections.containsKey(viewerId)) {
                        initiateConnectionToViewer(session, viewerId, isAudioOnly)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        session.viewersRef?.addValueEventListener(session.viewersListener!!)
    }

    private fun initiateConnectionToViewer(session: HostSessionInfo, viewerId: String, isAudioOnly: Boolean) {
        val pc = createPeerConnection(session.sessionId, true, viewerId) ?: return
        session.viewerPeerConnections[viewerId] = pc
        
        // Create Data Channel
        val dcInit = DataChannel.Init()
        val dc = pc.createDataChannel("alerts", dcInit)
        setupDataChannel(dc)
        session.viewerDataChannels[viewerId] = dc

        synchronized(mediaLock) {
            try {
                localMediaStream?.id
            } catch (e: Exception) {
                localMediaStream = null
            }
            localMediaStream?.let { pc.addStream(it) }
        }
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isReleased || !session.viewerPeerConnections.containsKey(viewerId)) return@postDelayed

            val constraints = MediaConstraints()
            constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isAudioOnly) "false" else "true"))
            constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))

            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    pc.setLocalDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            writeOfferToFirebase(session.sessionId, sdp, viewerId)
                            listenForAnswer(session, viewerId)
                            listenForRemoteCandidates(session.sessionId, true, viewerId)
                        }
                    }, sdp)
                }
            }, constraints)
        }, 500L)
    }

    private fun startMedia(sink: VideoSink?, isAudioOnly: Boolean) {
        if (isReleased) return

        synchronized(mediaLock) {
            // Check if existing localMediaStream is disposed
            try {
                localMediaStream?.id
            } catch (e: Exception) {
                localMediaStream = null
            }

            if (localMediaStream == null) {
                localMediaStream = staticFactory?.createLocalMediaStream("shared_stream")
            }
            val lms = localMediaStream!!

            // Check if existing video track is disposed
            try {
                localVideoTrack?.enabled()
            } catch (e: Exception) {
                localVideoTrack = null
            }

            // Ensure Video Track (Lazy init if missing)
            if (!isAudioOnly && localVideoTrack == null) {
                try {
                    videoCapturer = createCameraCapturer()
                    videoSource = staticFactory?.createVideoSource(videoCapturer!!.isScreencast)
                    surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", staticEglBase!!.eglBaseContext)
                    videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
                    videoCapturer?.startCapture(1280, 720, 30)

                    localVideoTrack = staticFactory?.createVideoTrack("video0", videoSource)
                    localVideoTrack?.let { lms.addTrack(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting camera: ${e.message}")
                }
            }

            // Check if existing audio track is disposed
            try {
                localAudioTrack?.enabled()
            } catch (e: Exception) {
                localAudioTrack = null
            }

            // Ensure Audio Track (Lazy init if missing)
            if (localAudioTrack == null) {
                audioSource = staticFactory?.createAudioSource(MediaConstraints())
                localAudioTrack = staticFactory?.createAudioTrack("audio0", audioSource)
                localAudioTrack?.let { lms.addTrack(it) }
            }

            // Enable tracks if they were disabled (e.g. by a previous viewer role)
            try {
                localAudioTrack?.setEnabled(true)
                localVideoTrack?.setEnabled(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable tracks: ${e.message}")
            }

            sink?.let {
                try {
                    localVideoTrack?.addSink(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add sink: ${e.message}")
                }
            }
            Log.d(TAG, "📡 Shared Media initialized/resumed")
        }
    }

    fun connectAsViewer(
        sessionId: String,
        remoteSink: VideoSink?,
        viewerDeviceId: String,
        peerId: String = ""
    ) {
        if (viewerSessions.containsKey(sessionId)) {
            val session = viewerSessions[sessionId]!!
            remoteSink?.let {
                session.remoteVideoSink = it
                session.remoteVideoTrack?.addSink(it)
            }
            return
        }

        val session = ViewerSessionInfo(sessionId, viewerDeviceId, remoteSink)
        viewerSessions[sessionId] = session
        listenForSessionStatus(session)

        val viewerRef = database.getReference("sessions/$sessionId/viewers/$viewerDeviceId")
        val viewerData = mapOf(
            "deviceId" to viewerDeviceId,
            "peerId" to peerId.ifEmpty { viewerDeviceId },
            "name" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "model" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "status" to "Online",
            "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        viewerRef.setValue(viewerData)
        viewerRef.child("status").onDisconnect().setValue("Offline")
        viewerRef.child("answer").onDisconnect().removeValue()
        viewerRef.child("calleeCandidates").onDisconnect().removeValue()

        database.getReference("sessions/$sessionId/metadata/deviceId").get().addOnSuccessListener { snapshot ->
            val monitorId = snapshot.getValue(String::class.java)
            if (monitorId != null) {
                database.getReference("monitors/$monitorId/isOccupied").setValue(true)
                database.getReference("monitors/$monitorId/isOccupied").onDisconnect().setValue(false)
            }
        }

        session.peerConnection = createPeerConnection(sessionId, false, viewerDeviceId)
        ensureLocalMediaForViewer(session)

        listenForOffer(session)
        listenForRemoteCandidates(sessionId, false, viewerDeviceId)
    }

    private fun ensureLocalMediaForViewer(session: ViewerSessionInfo) {
        if (isReleased) return

        synchronized(mediaLock) {
            // Check if existing localMediaStream is disposed
            try {
                localMediaStream?.id
            } catch (e: Exception) {
                localMediaStream = null
            }

            if (localMediaStream == null) {
                localMediaStream = staticFactory?.createLocalMediaStream("shared_stream")
            }

            // Check if existing audio track is disposed
            try {
                localAudioTrack?.enabled()
            } catch (e: Exception) {
                localAudioTrack = null
            }

            if (localAudioTrack == null) {
                audioSource = staticFactory?.createAudioSource(MediaConstraints())
                localAudioTrack = staticFactory?.createAudioTrack("audio0", audioSource)
                try {
                    localAudioTrack?.setEnabled(false) // Initially muted for viewers
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to set initial audio state: ${e.message}")
                }
                localAudioTrack?.let { localMediaStream?.addTrack(it) }
            }
            localMediaStream?.let { session.peerConnection?.addStream(it) }
        }
    }

    private fun createPeerConnection(sessionId: String, isCaller: Boolean, viewerId: String): PeerConnection? {
        return staticFactory?.createPeerConnection(
            buildRtcConfig(),
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    sendCandidateToFirebase(sessionId, candidate, isCaller, viewerId)
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "ICE State for $sessionId ($viewerId): $state")
                    val connected = state == PeerConnection.IceConnectionState.CONNECTED || state == PeerConnection.IceConnectionState.COMPLETED
                    sessionConnectionStates[sessionId]?.postValue(connected)
                    updateOverallConnectionState()
                    onConnectionEvent?.invoke(sessionId, isCaller, viewerId, connected)
                }

                override fun onTrack(transceiver: RtpTransceiver) {
                    val track = transceiver.receiver.track()
                    if (track is VideoTrack) {
                        val session = viewerSessions[sessionId]
                        session?.remoteVideoTrack = track
                        session?.remoteVideoSink?.let { track.addSink(it) }
                    }
                }

                override fun onAddStream(stream: MediaStream) {
                    if (stream.videoTracks.isNotEmpty()) {
                        val track = stream.videoTracks[0]
                        val session = viewerSessions[sessionId]
                        session?.remoteVideoTrack = track
                        session?.remoteVideoSink?.let { track.addSink(it) }
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {}
                override fun onIceConnectionReceivingChange(b: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(channel: DataChannel) {
                    val session = viewerSessions[sessionId]
                    if (session != null) {
                        session.dataChannel = channel
                        setupDataChannel(channel)
                    }
                }
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver, streams: Array<MediaStream>) {}
            }
        )
    }

    private fun updateOverallConnectionState() {
        val anyHostConnected = hostSessions.values.any { host ->
            host.viewerPeerConnections.values.any { pc ->
                pc.iceConnectionState() == PeerConnection.IceConnectionState.CONNECTED ||
                pc.iceConnectionState() == PeerConnection.IceConnectionState.COMPLETED
            }
        }
        val anyViewerConnected = viewerSessions.values.any { viewer ->
            viewer.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.CONNECTED ||
            viewer.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.COMPLETED
        }
        _connectionState.postValue(anyHostConnected || anyViewerConnected)
    }

    private fun listenForOffer(session: ViewerSessionInfo) {
        session.offerRef = database.getReference("sessions/${session.sessionId}/viewers/${session.viewerDeviceId}/offer")
        session.offerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val type = snapshot.child("type").getValue(String::class.java)
                val sdp = snapshot.child("sdp").getValue(String::class.java)

                if (type != null && sdp != null && session.peerConnection?.remoteDescription == null) {
                    val offer = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                    session.peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            createAnswer(session)
                            drainPendingCandidates(session.sessionId, null)
                        }
                    }, offer)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        session.offerRef?.addValueEventListener(session.offerListener!!)
    }

    private fun createAnswer(session: ViewerSessionInfo) {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        session.peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                session.peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        writeAnswerToFirebase(session.sessionId, sdp, session.viewerDeviceId)
                    }
                }, sdp)
            }
        }, constraints)
    }

    private fun writeOfferToFirebase(sessionId: String, sdp: SessionDescription, viewerId: String) {
        val data = mapOf("type" to sdp.type.canonicalForm(), "sdp" to sdp.description)
        database.getReference("sessions/$sessionId/viewers/$viewerId/offer").setValue(data)
    }

    private fun writeAnswerToFirebase(sessionId: String, sdp: SessionDescription, viewerId: String) {
        val data = mapOf("type" to sdp.type.canonicalForm(), "sdp" to sdp.description)
        database.getReference("sessions/$sessionId/viewers/$viewerId/answer").setValue(data)
    }

    private fun listenForAnswer(session: HostSessionInfo, viewerId: String) {
        val ref = database.getReference("sessions/${session.sessionId}/viewers/$viewerId/answer")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val type = snapshot.child("type").getValue(String::class.java)
                val sdp = snapshot.child("sdp").getValue(String::class.java)
                val pc = session.viewerPeerConnections[viewerId]
                if (type != null && sdp != null && pc != null && pc.remoteDescription == null) {
                    val answer = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                    pc.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            drainPendingCandidates(session.sessionId, viewerId)
                        }
                    }, answer)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        session.viewerAnswerListeners[viewerId] = listener
    }

    private fun sendCandidateToFirebase(sessionId: String, candidate: IceCandidate, isCaller: Boolean, viewerId: String) {
        val path = if (isCaller) "callerCandidates" else "calleeCandidates"
        val data = mapOf("candidate" to candidate.sdp, "sdpMid" to candidate.sdpMid, "sdpMLineIndex" to candidate.sdpMLineIndex)
        database.getReference("sessions/$sessionId/viewers/$viewerId/$path").push().setValue(data)
    }

    private fun listenForRemoteCandidates(sessionId: String, isCaller: Boolean, viewerId: String) {
        val path = if (isCaller) "calleeCandidates" else "callerCandidates"
        val ref = database.getReference("sessions/$sessionId/viewers/$viewerId/$path")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val candidateStr = snapshot.child("candidate").getValue(String::class.java)
                val sdpMid = snapshot.child("sdpMid").getValue(String::class.java)
                val sdpMLineIndex = snapshot.child("sdpMLineIndex").getValue(Int::class.java)
                if (candidateStr != null && sdpMid != null && sdpMLineIndex != null) {
                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, candidateStr)
                    val pc: PeerConnection?
                    val pendingList: MutableList<IceCandidate>?
                    if (isCaller) {
                        val session = hostSessions[sessionId]
                        pc = session?.viewerPeerConnections?.get(viewerId)
                        pendingList = session?.viewerPendingCandidates?.getOrPut(viewerId) { mutableListOf() }
                    } else {
                        val session = viewerSessions[sessionId]
                        pc = session?.peerConnection
                        pendingList = session?.pendingCandidates
                    }
                    if (pc != null) {
                        if (pc.remoteDescription == null) pendingList?.add(candidate)
                        else pc.addIceCandidate(candidate)
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        if (isCaller) hostSessions[sessionId]?.viewerCandidatesListeners?.set(viewerId, listener)
        else viewerSessions[sessionId]?.candidatesListener = listener
    }

    private fun drainPendingCandidates(sessionId: String, viewerId: String?) {
        if (viewerId != null) {
            val session = hostSessions[sessionId] ?: return
            val pc = session.viewerPeerConnections[viewerId] ?: return
            session.viewerPendingCandidates[viewerId]?.forEach { pc.addIceCandidate(it) }
            session.viewerPendingCandidates.remove(viewerId)
        } else {
            val session = viewerSessions[sessionId] ?: return
            session.pendingCandidates.forEach { session.peerConnection?.addIceCandidate(it) }
            session.pendingCandidates.clear()
        }
    }

    private fun setupDataChannel(channel: DataChannel) {
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(l: Long) {}
            override fun onStateChange() {
                Log.d(TAG, "DataChannel state: ${channel.state()}")
            }
            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = buffer.data
                val bytes = ByteArray(data.remaining())
                data.get(bytes)
                val message = String(bytes)
                Log.d(TAG, "Received message: $message")
                onMessageReceived?.invoke(message)
            }
        })
    }

    fun sendMessage(message: String) {
        val bytes = message.toByteArray()
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.put(bytes)
        buffer.flip()
        val dataBuffer = DataChannel.Buffer(buffer, false)
        
        var sentCount = 0
        // Host sends to all viewers
        hostSessions.values.forEach { session ->
            session.viewerDataChannels.values.forEach { dc ->
                if (dc.state() == DataChannel.State.OPEN) {
                    dc.send(dataBuffer)
                    sentCount++
                }
            }
        }
        
        // Viewer sends to host
        viewerSessions.values.forEach { session ->
            session.dataChannel?.let { dc ->
                if (dc.state() == DataChannel.State.OPEN) {
                    dc.send(dataBuffer)
                    sentCount++
                }
            }
        }
        Log.d(TAG, "Sent message to $sentCount recipients: $message")
    }

    fun initSurfaceViewRenderer(renderer: SurfaceViewRenderer, isLocal: Boolean = true) {
        if (isLocal) this.localRenderer = renderer
        try {
            staticEglBase?.let { egl ->
                renderer.init(egl.eglBaseContext, null)
                renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                renderer.setEnableHardwareScaler(true)
                renderer.setMirror(if (isLocal) isFrontFacing else false)
            }
        } catch (e: Exception) { Log.e(TAG, "Renderer init failed: ${e.message}") }
    }

    private fun createCameraCapturer(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        for (name in deviceNames) { if (enumerator.isFrontFacing(name)) { isFrontFacing = true; _isFrontFacing.postValue(true); return enumerator.createCapturer(name, null) } }
        for (name in deviceNames) { if (enumerator.isBackFacing(name)) { isFrontFacing = false; _isFrontFacing.postValue(false); return enumerator.createCapturer(name, null) } }
        throw RuntimeException("No camera available")
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFront: Boolean) { this@WebRTCManager.isFrontFacing = isFront; _isFrontFacing.postValue(isFront); localRenderer?.setMirror(isFront) }
            override fun onCameraSwitchError(error: String?) {}
        })
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        synchronized(mediaLock) {
            if (isReleased) return
            try {
                localAudioTrack?.setEnabled(enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting microphone enabled: ${e.message}")
            }
        }
    }

    fun addLocalSink(sink: VideoSink) {
        synchronized(mediaLock) {
            try {
                localVideoTrack?.addSink(sink)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding local sink: ${e.message}")
            }
        }
    }

    fun removeLocalSink(sink: VideoSink) {
        synchronized(mediaLock) {
            try {
                localVideoTrack?.removeSink(sink)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing local sink: ${e.message}")
            }
        }
    }

    fun addRemoteSink(sessionId: String, sink: VideoSink) {
        viewerSessions[sessionId]?.remoteVideoTrack?.addSink(sink)
    }

    fun removeRemoteSink(sink: VideoSink) {
        viewerSessions.values.forEach {
            try {
                it.remoteVideoTrack?.removeSink(sink)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing remote sink: ${e.message}")
            }
        }
    }
    fun getActiveVideoTrack(): VideoTrack? { return localVideoTrack ?: viewerSessions.values.firstOrNull()?.remoteVideoTrack }

    private fun listenForSessionStatus(session: Any) {
        val sid = when(session) { is HostSessionInfo -> session.sessionId; is ViewerSessionInfo -> session.sessionId; else -> return }
        val statusLiveData = sessionStatuses.getOrPut(sid) { MutableLiveData<String?>(null) }
        val ref = database.getReference("sessions/$sid/status")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                statusLiveData.postValue(status)
                _sessionStatus.postValue(status)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        if (session is HostSessionInfo) session.sessionListener = listener
        else if (session is ViewerSessionInfo) session.sessionListener = listener
    }

    fun stopHost(sessionId: String) { hostSessions.remove(sessionId)?.stop(); updateOverallConnectionState() }
    fun stopViewer(sessionId: String) { viewerSessions.remove(sessionId)?.stop(); updateOverallConnectionState() }

    fun resetViewerConnection(sessionId: String, viewerId: String) {
        val session = hostSessions[sessionId] ?: return
        
        // 1. Close and dispose PeerConnection
        session.viewerPeerConnections.remove(viewerId)?.let { pc ->
            try {
                pc.close()
                pc.dispose()
            } catch (e: Exception) {}
        }
        
        // 2. Remove listeners
        val answerRef = database.getReference("sessions/$sessionId/viewers/$viewerId/answer")
        session.viewerAnswerListeners.remove(viewerId)?.let { answerRef.removeEventListener(it) }
        
        val candidatesRef = database.getReference("sessions/$sessionId/viewers/$viewerId/calleeCandidates")
        session.viewerCandidatesListeners.remove(viewerId)?.let { candidatesRef.removeEventListener(it) }
        
        // 3. Clear data channel
        session.viewerDataChannels.remove(viewerId)
        
        // 4. Clear pending candidates
        session.viewerPendingCandidates.remove(viewerId)
        
        Log.d(TAG, "Reset viewer connection for $viewerId in session $sessionId")
    }

    fun release() {
        synchronized(mediaLock) {
            if (isReleased) return
            isReleased = true
            hostSessions.forEach { it.value.stop() }; viewerSessions.forEach { it.value.stop() }
            hostSessions.clear(); viewerSessions.clear()
            try {
                videoCapturer?.stopCapture()
            } catch (e: Exception) {
            }
            try {
                localVideoTrack?.dispose()
                localAudioTrack?.dispose()
                localMediaStream?.dispose()
                videoSource?.dispose()
                audioSource?.dispose()
                videoCapturer?.dispose()
                surfaceTextureHelper?.dispose()
            } catch (e: Exception) {
            }
            localVideoTrack = null; localAudioTrack = null; localMediaStream = null; videoSource = null; audioSource = null; videoCapturer = null; surfaceTextureHelper = null
            _connectionState.postValue(false)
        }
    }

    fun takeScreenshot(context: Context, isLocal: Boolean = true): String? {
        val renderer = if (isLocal) localRenderer else viewerSessions.values.firstOrNull()?.remoteVideoSink as? SurfaceViewRenderer
        if (renderer == null) return null
        var resultPath: String? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        renderer.addFrameListener({ bitmap ->
            try {
                val outputDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES), "Sentinel Captures")
                if (!outputDir.exists()) outputDir.mkdirs()
                val file = File(outputDir, "CAP_${System.currentTimeMillis()}.jpg")
                val out = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                out.close()
                resultPath = file.absolutePath
            } catch (e: Exception) {} finally { latch.countDown() }
        }, 1.0f)
        try { latch.await(2, java.util.concurrent.TimeUnit.SECONDS) } catch (e: Exception) {}
        return resultPath
    }

    interface OfferCallback { fun onOfferCreated(sdp: String) }
    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {}
        override fun onSetFailure(error: String?) {}
    }
}
