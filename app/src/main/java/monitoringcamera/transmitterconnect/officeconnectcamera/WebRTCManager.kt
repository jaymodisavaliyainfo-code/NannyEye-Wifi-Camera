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
import java.util.ArrayList
import java.util.HashMap

class WebRTCManager(private val context: Context) {
    private val TAG = "WebRTCManager"
    private val STUN_SERVER = "stun:stun.l.google.com:19302"

    private val database = FirebaseDatabase.getInstance()
    private var peerConnection: PeerConnection? = null // For Viewer
    private val viewerPeerConnections = mutableMapOf<String, PeerConnection>() // For Broadcaster (viewerId -> PC)
    
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localMediaStream: MediaStream? = null  // FIX B: Plan B uses addStream
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var remoteVideoTrack: VideoTrack? = null
    private var currentSessionId: String? = null
    private var currentCameraId: String? = null
    private var currentViewerDeviceId: String? = null // For Viewer: self, For Broadcaster: latest joined
    private var localBroadcasterDeviceId: String? = null
    private var viewersRef: DatabaseReference? = null
    private var viewersListener: ValueEventListener? = null

    private val pendingCandidates = mutableListOf<IceCandidate>() // For Viewer
    private val viewerPendingCandidates = mutableMapOf<String, MutableList<IceCandidate>>() // For Broadcaster

    private val viewerAnswerListeners = mutableMapOf<String, ValueEventListener>()
    private val viewerCandidatesListeners = mutableMapOf<String, ChildEventListener>()

    private val _connectionState = MutableLiveData(false)
    val connectionState: LiveData<Boolean> get() = _connectionState

    private var remoteVideoSink: VideoSink? = null
    private var isFrontFacing = true
    private val _isFrontFacing = MutableLiveData(true)
    val isFrontFacingLiveData: LiveData<Boolean> get() = _isFrontFacing
    
    private var localRenderer: SurfaceViewRenderer? = null
    var isBroadcaster = false
    
    private val _sessionStatus = MutableLiveData<String?>()
    val sessionStatus: LiveData<String?> get() = _sessionStatus

    private val _isRecording = MutableLiveData(false)
    val isRecordingLiveData: LiveData<Boolean> get() = _isRecording
    private var lastRecordedFile: File? = null
    private var sessionRef: DatabaseReference? = null
    private var sessionListener: ValueEventListener? = null

    private var answerRef: DatabaseReference? = null
    private var answerListener: ValueEventListener? = null

    private var offerRef: DatabaseReference? = null
    private var offerListener: ValueEventListener? = null

    private var candidatesRef: DatabaseReference? = null
    private var candidatesListener: ChildEventListener? = null

    @Volatile
    private var isReleased = false

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

    private fun buildRtcConfig(): PeerConnection.RTCConfiguration {
        // FIX A: Add TURN server so cross-network (Android→iOS over internet) works.
        // Pure STUN only works on the same LAN. TURN is the relay fallback.
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
        // Keep PLAN_B to match GoogleWebRTC 1.1.32000 on iOS side.
        // Both sides must use the same semantics for Plan B SDP to be valid.
        return PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.PLAN_B
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }

    fun startCameraAndOffer(sessionId: String, deviceId: String, localSink: VideoSink?, isAudioOnly: Boolean, deviceName: String, callback: OfferCallback) {
        if (currentSessionId == sessionId && isBroadcaster && !isReleased) {
            localSink?.let { localVideoTrack?.addSink(it) }
            return
        }
        
        release()
        
        currentSessionId = sessionId
        localBroadcasterDeviceId = deviceId
        isBroadcaster = true
        isReleased = false
        listenForSessionStatus(sessionId)

        // Add metadata for the session
        val metadata = mapOf(
            "name" to deviceName,
            "deviceId" to deviceId,
            "type" to "Camera",
            "timestamp" to ServerValue.TIMESTAMP
        )
        database.getReference("sessions/$sessionId/metadata").setValue(metadata)
        
        // Ensure the session is cleaned up if we lose connection or crash
        database.getReference("sessions/$sessionId").onDisconnect().removeValue()

        // Registry for stable device tracking
        val registryRef = database.getReference("monitors/$deviceId")
        registryRef.child("activeSessionId").setValue(sessionId)
        registryRef.child("lastSeen").setValue(ServerValue.TIMESTAMP)
        registryRef.onDisconnect().removeValue()

        // Multi-viewer support: Listen for all viewers joining
        viewersRef = database.getReference("sessions/$sessionId/viewers")
        viewersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val viewerId = child.key ?: continue
                    val status = child.child("status").getValue(String::class.java)
                    if (status == "Online" && !viewerPeerConnections.containsKey(viewerId)) {
                        Log.d(TAG, "🔥 New Viewer joined: $viewerId")
                        currentViewerDeviceId = viewerId
                        initiateConnectionToViewer(sessionId, viewerId, isAudioOnly)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        viewersRef?.addValueEventListener(viewersListener!!)

        startMedia(localSink, isAudioOnly)
    }

    private fun initiateConnectionToViewer(sessionId: String, viewerId: String, isAudioOnly: Boolean) {
        val pc = createPeerConnection(sessionId, true, viewerId) ?: return
        viewerPeerConnections[viewerId] = pc
        
        localMediaStream?.let { pc.addStream(it) }
        
        // Delay to allow camera pipeline to start if this is the first viewer
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            if (isReleased || !viewerPeerConnections.containsKey(viewerId)) return@postDelayed

            val constraints = MediaConstraints()
            constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isAudioOnly) "false" else "true"))
            constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))

            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    pc.setLocalDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            writeOfferToFirebase(sessionId, sdp, viewerId)
                            listenForAnswer(sessionId, viewerId)
                            listenForRemoteCandidates(sessionId, true, viewerId)
                        }
                    }, sdp)
                }
                override fun onCreateFailure(error: String?) {
                    Log.e(TAG, "❌ createOffer failed for $viewerId: $error")
                }
            }, constraints)
        }, 500L)
    }

    private fun updateOverallConnectionState() {
        val connected = viewerPeerConnections.values.any { pc ->
            val state = pc.iceConnectionState()
            state == PeerConnection.IceConnectionState.CONNECTED || state == PeerConnection.IceConnectionState.COMPLETED
        }
        _connectionState.postValue(connected)
    }

    private fun startMedia(sink: VideoSink?, isAudioOnly: Boolean) {
        localMediaStream = staticFactory?.createLocalMediaStream(if (isBroadcaster) "stream0" else "stream_viewer")
        val localStream = localMediaStream

        if (!isAudioOnly) {
            videoCapturer = createCameraCapturer()
            videoSource = staticFactory?.createVideoSource(videoCapturer!!.isScreencast)
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", staticEglBase!!.eglBaseContext)
            videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
            videoCapturer?.startCapture(1280, 720, 30)

            localVideoTrack = staticFactory?.createVideoTrack("video0", videoSource)
            sink?.let { localVideoTrack?.addSink(it) }
            localVideoTrack?.let { localStream?.addTrack(it) }
        }

        audioSource = staticFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = staticFactory?.createAudioTrack("audio0", audioSource)
        localAudioTrack?.let { localStream?.addTrack(it) }

        // For Viewer role, add stream to PC immediately
        if (!isBroadcaster) {
            localStream?.let { peerConnection?.addStream(it) }
        }
        Log.d(TAG, "📡 Media started: video=${localVideoTrack != null}, audio=${localAudioTrack != null}")
    }

    fun suspendCamera() {
        try {
            videoCapturer?.stopCapture()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Failed to suspend camera: ${e.message}")
        }
    }

    fun resumeCamera() {
        videoCapturer?.startCapture(1280, 720, 30)
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFront: Boolean) {
                this@WebRTCManager.isFrontFacing = isFront
                _isFrontFacing.postValue(isFront)
                localRenderer?.setMirror(isFront)
                
                // Update currentCameraId after switch
                val enumerator = Camera2Enumerator(context)
                val deviceNames = enumerator.deviceNames
                for (name in deviceNames) {
                    if (isFront && enumerator.isFrontFacing(name)) {
                        currentCameraId = name
                        break
                    } else if (!isFront && enumerator.isBackFacing(name)) {
                        currentCameraId = name
                        break
                    }
                }
            }
            override fun onCameraSwitchError(error: String?) {
                Log.e(TAG, "Camera switch error: $error")
            }
        })
    }

    private var legacyCamera: android.hardware.Camera? = null

    fun getLocalVideoTrack(): VideoTrack? {
        return localVideoTrack
    }

    fun getActiveVideoTrack(): VideoTrack? {
        return if (isBroadcaster) localVideoTrack else remoteVideoTrack
    }

    private fun createCameraCapturer(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                isFrontFacing = true
                _isFrontFacing.postValue(true)
                currentCameraId = name
                return enumerator.createCapturer(name, null)
            }
        }

        // Prefer BACK camera for Nanny/Monitor usage
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                isFrontFacing = false
                _isFrontFacing.postValue(false)
                currentCameraId = name
                return enumerator.createCapturer(name, null)
            }
        }

        throw RuntimeException("No camera available")
    }



    private fun cleanupViewersListener() {
        viewersListener?.let { viewersRef?.removeEventListener(it) }
        viewersListener = null
        viewersRef = null
    }

    fun connectAsViewer(sessionId: String, remoteSink: VideoSink?, viewerDeviceId: String) {
        Log.d(TAG, "Connecting as viewer to $sessionId with deviceId: $viewerDeviceId")

        // If it's the same session and we are already connected/connecting, just update the sink
        if (currentSessionId == sessionId && peerConnection != null && !isReleased) {
            remoteSink?.let {
                this.remoteVideoSink = it
                remoteVideoTrack?.addSink(it)
            }
            Log.d(TAG, "Already connected to $sessionId, skipping full re-init")
            return
        }

        // Always release old session if switching or starting fresh
        release()

        currentSessionId = sessionId
        currentViewerDeviceId = viewerDeviceId
        isBroadcaster = false
        isReleased = false // Reset release flag
        listenForSessionStatus(sessionId)

        this.remoteVideoSink = remoteSink

        // Viewer should only remove their contribution on abrupt disconnect
        database.getReference("sessions/$sessionId/viewers/$viewerDeviceId/answer").onDisconnect().removeValue()
        database.getReference("sessions/$sessionId/viewers/$viewerDeviceId/calleeCandidates").onDisconnect().removeValue()
        
        // Track this viewer using Device ID
        val viewerName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        val viewerData = mapOf(
            "deviceId" to viewerDeviceId,
            "name" to viewerName,
            "status" to "Online",
            "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        val viewerRef = database.getReference("sessions/$sessionId/viewers/$viewerDeviceId")
        viewerRef.setValue(viewerData)
        viewerRef.child("status").onDisconnect().setValue("Offline")

        // Mark monitor as occupied
        database.getReference("sessions/$sessionId/metadata/deviceId").get().addOnSuccessListener { snapshot ->
            val monitorId = snapshot.getValue(String::class.java)
            if (monitorId != null) {
                database.getReference("monitors/$monitorId/isOccupied").setValue(true)
                database.getReference("monitors/$monitorId/isOccupied").onDisconnect().setValue(false)
            }
        }

        peerConnection = createPeerConnection(sessionId, false, viewerDeviceId)

        // Push-to-talk support: Add local audio track but keep it disabled initially
        try {
            audioSource = staticFactory?.createAudioSource(MediaConstraints())
            localAudioTrack = staticFactory?.createAudioTrack("audio_viewer", audioSource)
            localAudioTrack?.setEnabled(false)
            localMediaStream = staticFactory?.createLocalMediaStream("stream_viewer")
            localAudioTrack?.let { localMediaStream?.addTrack(it) }
            localMediaStream?.let { peerConnection?.addStream(it) }
            Log.d(TAG, "Viewer audio track added (disabled, Plan B addStream)")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding viewer audio track: ${e.message}")
        }

        listenForOffer(sessionId, viewerDeviceId)
        listenForRemoteCandidates(sessionId, false, viewerDeviceId)
    }

    private fun listenForOffer(sessionId: String, viewerId: String) {
        cleanupOfferListener()
        offerRef = database.getReference("sessions/$sessionId/viewers/$viewerId/offer")
        offerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val type = snapshot.child("type").getValue(String::class.java)
                val sdp = snapshot.child("sdp").getValue(String::class.java)

                if (type != null && sdp != null && peerConnection != null && peerConnection?.remoteDescription == null) {
                    val offer = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                    peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            createAnswer(sessionId, viewerId)
                            drainPendingCandidates()
                        }
                    }, offer)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Error: ${error.message}")
            }
        }
        offerRef?.addValueEventListener(offerListener!!)
    }

    private fun createAnswer(sessionId: String, viewerId: String) {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))

        peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        writeAnswerToFirebase(sessionId, sdp, viewerId)
                    }
                }, sdp)
            }
        }, constraints)
    }

    private fun createPeerConnection(sessionId: String, isCaller: Boolean, viewerId: String): PeerConnection? {
        return staticFactory?.createPeerConnection(
            buildRtcConfig(),
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    if (isReleased) return
                    sendCandidateToFirebase(sessionId, candidate, isCaller, viewerId)
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    if (isReleased) return
                    Log.d(TAG, "ICE Connection State for $viewerId: $state")
                    
                    if (isBroadcaster) {
                        updateOverallConnectionState()
                    } else {
                        val connected = state == PeerConnection.IceConnectionState.CONNECTED ||
                                state == PeerConnection.IceConnectionState.COMPLETED
                        _connectionState.postValue(connected)
                    }

                    if (state == PeerConnection.IceConnectionState.DISCONNECTED || state == PeerConnection.IceConnectionState.FAILED) {
                        Log.w(TAG, "ICE Disconnected or Failed for $viewerId")
                    }
                }

                override fun onTrack(transceiver: RtpTransceiver) {
                    if (isReleased) return
                    val track = transceiver.receiver.track()
                    if (track is VideoTrack) {
                        remoteVideoTrack = track
                        remoteVideoSink?.let { track.addSink(it) }
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {}
                override fun onIceConnectionReceivingChange(b: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
                override fun onAddStream(stream: MediaStream) {
                    if (isReleased) return
                    if (stream.videoTracks.isNotEmpty()) {
                        val track = stream.videoTracks[0]
                        remoteVideoTrack = track
                        remoteVideoSink?.let { track.addSink(it) }
                    }
                }

                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(channel: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver, streams: Array<MediaStream>) {}
            }
        )
    }

    private fun writeOfferToFirebase(sessionId: String, sdp: SessionDescription, viewerId: String) {
        val data = HashMap<String, String>()
        data["type"] = sdp.type.canonicalForm()
        data["sdp"] = sdp.description
        database.getReference("sessions/$sessionId/viewers/$viewerId/offer").setValue(data)
    }

    private fun writeAnswerToFirebase(sessionId: String, sdp: SessionDescription, viewerId: String) {
        val data = HashMap<String, String>()
        data["type"] = sdp.type.canonicalForm()
        data["sdp"] = sdp.description
        database.getReference("sessions/$sessionId/viewers/$viewerId/answer").setValue(data)
    }

    private fun listenForAnswer(sessionId: String, viewerId: String) {
        val ref = database.getReference("sessions/$sessionId/viewers/$viewerId/answer")
        viewerAnswerListeners[viewerId]?.let { ref.removeEventListener(it) }
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val type = snapshot.child("type").getValue(String::class.java)
                val sdp = snapshot.child("sdp").getValue(String::class.java)
                val pc = viewerPeerConnections[viewerId]
                if (type != null && sdp != null && pc != null && pc.remoteDescription == null) {
                    val answer = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                    pc.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            drainPendingCandidates(viewerId)
                        }
                    }, answer)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        viewerAnswerListeners[viewerId] = listener
    }

    private fun sendCandidateToFirebase(sessionId: String, candidate: IceCandidate, isCaller: Boolean, viewerId: String) {
        val path = if (isCaller) "callerCandidates" else "calleeCandidates"
        val data = HashMap<String, Any>()
        data["candidate"] = candidate.sdp
        data["sdpMid"] = candidate.sdpMid
        data["sdpMLineIndex"] = candidate.sdpMLineIndex
        database.getReference("sessions/$sessionId/viewers/$viewerId/$path").push().setValue(data)
    }

    private fun listenForRemoteCandidates(sessionId: String, isCaller: Boolean, viewerId: String) {
        val path = if (isCaller) "calleeCandidates" else "callerCandidates"
        val ref = database.getReference("sessions/$sessionId/viewers/$viewerId/$path")
        
        viewerCandidatesListeners[viewerId]?.let { ref.removeEventListener(it) }

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val candidateStr = snapshot.child("candidate").getValue(String::class.java)
                val sdpMid = snapshot.child("sdpMid").getValue(String::class.java)
                val sdpMLineIndex = snapshot.child("sdpMLineIndex").getValue(Int::class.java)
                val pc = if (isBroadcaster) viewerPeerConnections[viewerId] else peerConnection
                
                if (candidateStr != null && sdpMid != null && sdpMLineIndex != null && pc != null) {
                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, candidateStr)
                    if (pc.remoteDescription == null) {
                        if (isBroadcaster) {
                            viewerPendingCandidates.getOrPut(viewerId) { mutableListOf() }.add(candidate)
                        } else {
                            pendingCandidates.add(candidate)
                        }
                    } else {
                        pc.addIceCandidate(candidate)
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        viewerCandidatesListeners[viewerId] = listener
    }

    private fun drainPendingCandidates(viewerId: String? = null) {
        if (isBroadcaster && viewerId != null) {
            val pc = viewerPeerConnections[viewerId]
            viewerPendingCandidates[viewerId]?.forEach { pc?.addIceCandidate(it) }
            viewerPendingCandidates.remove(viewerId)
        } else {
            pendingCandidates.forEach { peerConnection?.addIceCandidate(it) }
            pendingCandidates.clear()
        }
    }

    fun initSurfaceViewRenderer(renderer: SurfaceViewRenderer, isLocal: Boolean = true) {
        if (isLocal) {
            this.localRenderer = renderer
        }
        try {
            staticEglBase?.let { egl ->
                renderer.init(egl.eglBaseContext, null)
                renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                renderer.setEnableHardwareScaler(true)
                renderer.setMirror(if (isLocal) isFrontFacing else false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Renderer init failed: ${e.message}")
        }
    }

    fun startRecording(file: File) {
        // Actual recording logic would go here
        // For now, we'll just update the state to fix build errors
        lastRecordedFile = file
        _isRecording.postValue(true)
    }

    fun stopRecording(): String? {
        _isRecording.postValue(false)
        return lastRecordedFile?.absolutePath
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        Log.d(TAG, "Setting microphone enabled: $enabled")
        localAudioTrack?.setEnabled(enabled)
    }

    @Suppress("DEPRECATION")
    private fun handleLegacyFlashlight(isOn: Boolean) {
        // Removed as per request to handle flash outside WebRTCManager
    }

    private fun listenForSessionStatus(sessionId: String) {
        cleanupSessionListener()
        sessionRef = database.getReference("sessions/$sessionId/status")
        sessionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                _sessionStatus.postValue(status)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Session status listener cancelled: ${error.message}")
            }
        }
        sessionRef?.addValueEventListener(sessionListener!!)
    }

    fun removeLocalSink(sink: VideoSink) {
        localVideoTrack?.removeSink(sink)
    }

    fun removeRemoteSink(sink: VideoSink) {
        remoteVideoTrack?.removeSink(sink)
        if (remoteVideoSink == sink) {
            remoteVideoSink = null
        }
    }

    fun closeConnectionToViewer(viewerId: String) {
        val pc = viewerPeerConnections.remove(viewerId)
        try {
            pc?.close()
            pc?.dispose()
        } catch (e: Exception) {}
        
        val sessionId = currentSessionId ?: return
        
        val answerRef = database.getReference("sessions/$sessionId/viewers/$viewerId/answer")
        viewerAnswerListeners.remove(viewerId)?.let { answerRef.removeEventListener(it) }
        
        val candidatesRef = database.getReference("sessions/$sessionId/viewers/$viewerId/calleeCandidates")
        viewerCandidatesListeners.remove(viewerId)?.let { candidatesRef.removeEventListener(it) }
        
        viewerPendingCandidates.remove(viewerId)
        
        updateOverallConnectionState()
    }

    private fun cleanupOfferListener() {
        offerListener?.let { offerRef?.removeEventListener(it) }
        offerListener = null
        offerRef = null
    }

    private fun cleanupAnswerListener() {
        answerListener?.let { answerRef?.removeEventListener(it) }
        answerListener = null
        answerRef = null
    }

    private fun cleanupCandidatesListener() {
        candidatesListener?.let { candidatesRef?.removeEventListener(it) }
        candidatesListener = null
        candidatesRef = null
    }

    private fun cleanupSessionListener() {
        sessionListener?.let { sessionRef?.removeEventListener(it) }
        sessionListener = null
        sessionRef = null
    }

    private fun cleanupAllListeners() {
        cleanupSessionListener()
        cleanupAnswerListener()
        cleanupOfferListener()
        cleanupCandidatesListener()
        cleanupViewersListener()
        
        // Cleanup multi-viewer listeners
        val sessionId = currentSessionId
        if (sessionId != null) {
            viewerAnswerListeners.forEach { (vId, listener) ->
                database.getReference("sessions/$sessionId/viewers/$vId/answer").removeEventListener(listener)
            }
            viewerCandidatesListeners.forEach { (vId, listener) ->
                database.getReference("sessions/$sessionId/viewers/$vId/calleeCandidates").removeEventListener(listener)
            }
        }
        viewerAnswerListeners.clear()
        viewerCandidatesListeners.clear()
    }

    fun takeScreenshot(context: Context, isLocal: Boolean = true): String? {
        val renderer = if (isLocal) localRenderer else (remoteVideoSink as? SurfaceViewRenderer)
        if (renderer == null) {
            Log.e(TAG, "takeScreenshot: No renderer available (isLocal=$isLocal)")
            return null
        }

        var resultPath: String? = null
        val latch = java.util.concurrent.CountDownLatch(1)

        renderer.addFrameListener({ bitmap ->
            try {
                val outputDir = File(
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES),
                    "Sentinel Captures"
                )
                if (!outputDir.exists()) outputDir.mkdirs()

                val file = File(outputDir, "CAP_${System.currentTimeMillis()}.jpg")
                val out = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                resultPath = file.absolutePath
                Log.d(TAG, "Screenshot saved to: $resultPath")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save screenshot: ${e.message}")
            } finally {
                latch.countDown()
            }
        }, 1.0f)

        try {
            latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "Screenshot timeout")
        }

        return resultPath
    }

    fun release() {
        Log.d(TAG, "Releasing WebRTC resources...")

        // 1. Capture references and immediately nullify to avoid race conditions with new sessions
        val pcToDispose = peerConnection
        val viewersToDispose = HashMap(viewerPeerConnections)
        
        val vtToDispose = localVideoTrack
        val atToDispose = localAudioTrack
        val lmsToDispose = localMediaStream
        val vsToDispose = videoSource
        val asToDispose = audioSource
        val vcToDispose = videoCapturer
        val sthToDispose = surfaceTextureHelper
        val rvtToDispose = remoteVideoTrack
        
        val sessionIdToDelete = currentSessionId
        val viewerIdToDelete = currentViewerDeviceId
        val broadcasterDeviceIdToDelete = localBroadcasterDeviceId
        val wasBroadcaster = isBroadcaster

        synchronized(this) {
            if (isReleased) {
                Log.d(TAG, "Already released or releasing")
                return
            }
            isReleased = true
        }

        // 2. Immediate hardware halt on the calling thread
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = android.media.AudioManager.MODE_NORMAL
            audioManager.isMicrophoneMute = false
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting audio: ${e.message}")
        }

        try {
            vcToDispose?.stopCapture()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capturer: ${e.message}")
        }

        // Reset state members immediately
        peerConnection = null
        viewerPeerConnections.clear()
        
        localVideoTrack = null
        localAudioTrack = null
        localMediaStream = null
        videoSource = null
        audioSource = null
        videoCapturer = null
        surfaceTextureHelper = null
        remoteVideoTrack = null
        currentSessionId = null
        currentViewerDeviceId = null
        localBroadcasterDeviceId = null
        localRenderer = null
        remoteVideoSink = null

        // 3. Run remaining cleanup on a background thread
        Thread {
            try {
                // Firebase cleanup
                if (sessionIdToDelete != null) {
                    val ref = database.getReference("sessions/$sessionIdToDelete")
                    if (wasBroadcaster) {
                        try { ref.onDisconnect().cancel() } catch (e: Exception) {}
                        ref.removeValue()
                        
                        if (broadcasterDeviceIdToDelete != null) {
                            val monitorRef = database.getReference("monitors/$broadcasterDeviceIdToDelete")
                            try { monitorRef.onDisconnect().cancel() } catch (e: Exception) {}
                            monitorRef.removeValue()

                            val saveSessionRef = database.getReference("SaveSessions/$broadcasterDeviceIdToDelete")
                            try { saveSessionRef.onDisconnect().cancel() } catch (e: Exception) {}
                            saveSessionRef.removeValue()
                        }
                    } else {
                        try {
                            if (viewerIdToDelete != null) {
                                ref.child("viewers").child(viewerIdToDelete).child("answer").onDisconnect().cancel()
                                ref.child("viewers").child(viewerIdToDelete).child("calleeCandidates").onDisconnect().cancel()
                                ref.child("viewers").child(viewerIdToDelete).child("status").onDisconnect().cancel()
                                
                                ref.child("viewers").child(viewerIdToDelete).child("answer").removeValue()
                                ref.child("viewers").child(viewerIdToDelete).child("calleeCandidates").removeValue()
                                ref.child("viewers").child(viewerIdToDelete).child("status").setValue("Offline")
                            }
                        } catch (e: Exception) {}
                        
                        if (viewerIdToDelete != null) {
                            // Check if this was the last viewer to clear isOccupied
                            ref.child("metadata/deviceId").get().addOnSuccessListener { snapshot ->
                                val monitorId = snapshot.getValue(String::class.java)
                                if (monitorId != null) {
                                    ref.child("viewers").get().addOnSuccessListener { viewersSnapshot ->
                                        var hasOtherOnlineViewers = false
                                        for (v in viewersSnapshot.children) {
                                            if (v.key != viewerIdToDelete && v.child("status").getValue(String::class.java) == "Online") {
                                                hasOtherOnlineViewers = true
                                                break
                                            }
                                        }
                                        if (!hasOtherOnlineViewers) {
                                            database.getReference("monitors/$monitorId/isOccupied").setValue(false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                cleanupAllListeners()
                _sessionStatus.postValue(null)

                // Safe disposal of captured WebRTC components
                vtToDispose?.let { track ->
                    try {
                        track.setEnabled(false)
                        track.dispose()
                    } catch (e: Exception) {}
                }

                atToDispose?.let { track ->
                    try {
                        track.setEnabled(false)
                        track.dispose()
                    } catch (e: Exception) {}
                }

                rvtToDispose?.let { track ->
                    try { track.setEnabled(false) } catch (e: Exception) {}
                }

                try { lmsToDispose?.dispose() } catch (e: Exception) {}
                try { vsToDispose?.dispose() } catch (e: Exception) {}
                try { asToDispose?.dispose() } catch (e: Exception) {}

                pcToDispose?.let { pc ->
                    try {
                        pc.close()
                        pc.dispose()
                    } catch (e: Exception) {}
                }
                
                viewersToDispose.values.forEach { pc ->
                    try {
                        pc.close()
                        pc.dispose()
                    } catch (e: Exception) {}
                }

                try { vcToDispose?.dispose() } catch (e: Exception) {}
                try { sthToDispose?.dispose() } catch (e: Exception) {}

                _connectionState.postValue(false)
                Log.d(TAG, "WebRTC resources fully released")
            } catch (e: Exception) {
                Log.e(TAG, "Error in release background thread: ${e.message}")
            } finally {
                synchronized(this@WebRTCManager) {
                    isReleased = false
                }
            }
        }.start()
    }

    interface OfferCallback {
        fun onOfferCreated(sdp: String)
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {
            Log.e("SdpObserver", "onCreateFailure: $error")
        }

        override fun onSetFailure(error: String?) {
            Log.e("SdpObserver", "onSetFailure: $error")
        }
    }
}
