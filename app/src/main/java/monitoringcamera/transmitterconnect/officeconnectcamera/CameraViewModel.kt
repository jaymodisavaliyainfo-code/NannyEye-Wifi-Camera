package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "CameraViewModel"

    val webRTCManager: WebRTCManager = WebRTCManager(application)
    private val audioManager: AudioManager =
        application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val myDeviceId = MutableLiveData<String>("")
    val viewerDeviceId = MutableLiveData<String>("")
    val sessionId = MutableLiveData<String>("")
    val roomId = MutableLiveData<String>("")
    val hostId = MutableLiveData<String>("")
    val deviceName = MutableLiveData<String>("")
    val qrBitmap = MutableLiveData<Bitmap>()
    val activePreviewSessionIds = MutableLiveData<Set<String>>(emptySet())
    val activePreviewDeviceNames = MutableLiveData<Map<String, String>>(emptyMap())
    val isConnected: LiveData<Boolean> = webRTCManager.connectionState
    private val _isBroadcasting = MutableLiveData(false)
    val isBroadcasting: LiveData<Boolean> = _isBroadcasting
    val isRemoteConnected: LiveData<Boolean> = webRTCManager.connectionState
    val sessionStatus: LiveData<String?> = webRTCManager.sessionStatus
    val isFrontFacing: LiveData<Boolean> = webRTCManager.isFrontFacingLiveData

    // Motion Detection
    val motionDetected: LiveData<Boolean> = webRTCManager.motionDetected
    val personDetected: LiveData<Boolean> = webRTCManager.personDetected
    private lateinit var alertManager: AlertManager
    private var openCvDetector: OpenCvDetector? = null
    private var mlKitDetector: MlKitDetector = MlKitDetector()
    private var motionEnabled: Boolean = true
    private var personDetectionEnabled: Boolean = true
    private var localMotionSink: MotionDetectionSink? = null
    private var remoteMotionSink: MotionDetectionSink? = null
    private val personTracker = PersonTracker()

    private val _connectedViewers = MutableLiveData<List<ViewerInfo>>(emptyList())
    val connectedViewers: LiveData<List<ViewerInfo>> = _connectedViewers

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = DeviceDatabase.getInstance(application)
    private val cameraActivityDao = database.cameraActivityDao()
    private val pairedDeviceDao = database.pairedDeviceDao()
    private val connectedViewerDao = database.connectedViewerDao()
    private var ipCamerasListener: ListenerRegistration? = null
    private val loggedViewerSessions = mutableSetOf<String>()
    private var lastLoggedActivity: Pair<String, String>? = null
    private var lastLogTimestamp: Long = 0

    // Persistent storage for settings and limits (kept for device-specific values)
    private val settingsPrefs =
        application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val appPrefs =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val limitPrefs =
        application.getSharedPreferences("DailyLimitPrefs", Context.MODE_PRIVATE)

    private val _savedDevices = MutableLiveData<List<PairedDevice>>(emptyList())
    val savedDevices: LiveData<List<PairedDevice>> = _savedDevices

    private val _savedViewers = MutableLiveData<List<ConnectedViewer>>(emptyList())
    val savedViewers: LiveData<List<ConnectedViewer>> = _savedViewers

    private val _roomDevices = MutableLiveData<List<Device>>(emptyList())
    val roomDevices: LiveData<List<Device>> = _roomDevices

    private val _videoRecords = MutableLiveData<List<VideoRecord>>(emptyList())
    val videoRecords: LiveData<List<VideoRecord>> = _videoRecords

    private val _cameraActivities = MutableLiveData<List<CameraActivity>>(emptyList())
    val cameraActivities: LiveData<List<CameraActivity>> = _cameraActivities

    private val _isLoadingVideos = MutableLiveData(false)
    val isLoadingVideos: LiveData<Boolean> = _isLoadingVideos

    private val _usedSecondsToday = MutableLiveData(0)
    val usedSecondsToday: LiveData<Int> = _usedSecondsToday

    private val _dailyLimitSeconds = MutableLiveData(300)
    val dailyLimitSeconds: LiveData<Int> = _dailyLimitSeconds

    data class ViewerInfo(
        val deviceId: String,
        val name: String,
        val status: String
    )

    data class VideoRecord(
        val file: File,
        val name: String,
        val formattedDate: String,
        val duration: String,
        val thumbnail: Bitmap?,
        val size: String
    )

    // Own the recording LiveData here — WebRTCManager's isRecordingLiveData is just a placeholder stub.
    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _sessionSeconds = MutableLiveData(0)
    val sessionSeconds: LiveData<Int> = _sessionSeconds

    private var recorderManager: RecorderManager? = null
    var isAudio: Boolean = false

    private val _isPaired = MutableLiveData(false)
    val isPaired: LiveData<Boolean> = _isPaired

    private val _isCreator = MutableLiveData(false)
    val isCreator: LiveData<Boolean> = _isCreator

    data class MonitorInfo(
        val sessionId: String,
        val isOccupied: Boolean = false
    )

    data class PersistentSession(
        val sessionId: String = "",
        val roomId: String = "",
        val hostId: String = "",
        val connectionCode: String = "",
        val status: String = "active",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    data class ReconnectRequest(
        val requestId: String = "",
        val hostId: String = "",
        val viewerId: String = "",
        val roomId: String = "",
        val sessionId: String = "",
        val hostName: String = "",
        val status: String = "pending", // pending, accepted, declined
        val createdAt: Long = System.currentTimeMillis()
    )

    private val _onlineMonitors =
        MutableLiveData<Map<String, MonitorInfo>>(emptyMap()) // deviceId -> MonitorInfo
    val onlineMonitors: LiveData<Map<String, MonitorInfo>> = _onlineMonitors

    init {
        val id = settingsPrefs?.getString("my_device_id", null)
        val name =
            settingsPrefs?.getString("my_device_name", "${Build.MANUFACTURER} ${Build.MODEL}")
                ?: "${Build.MANUFACTURER} ${Build.MODEL}"

        var dId = settingsPrefs?.getString("my_device_id", null)
        if (dId == null) {
            dId = UUID.randomUUID().toString().replace("-", "").substring(0, 10)
            settingsPrefs?.edit()?.putString("my_device_id", dId)?.apply()
        }
        myDeviceId.value = dId
        deviceName.value = name

        _isPaired.value = settingsPrefs?.getBoolean("is_paired", false) ?: false
        _isCreator.value = settingsPrefs?.getBoolean("is_creator", false) ?: false

        setupFirestoreListeners()
        loadDailyLimit()
        observeLocalActivities()
        observeLocalDevices()
        
        // Ensure listener starts when UID is available
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                listenForFirestoreReconnectRequests()
            }
        }

        // Listen for all online monitors to enable stable reconnection
        FirebaseDatabase.getInstance().getReference("monitors")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = mutableMapOf<String, MonitorInfo>()
                    for (child in snapshot.children) {
                        val mId = child.key ?: continue
                        val sId =
                            child.child("activeSessionId").getValue(String::class.java) ?: continue
                        val occupied =
                            child.child("isOccupied").getValue(Boolean::class.java) ?: false
                        map[mId] = MonitorInfo(sId, occupied)
                    }
                    _onlineMonitors.postValue(map)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Initialize AlertManager and OpenCvDetector
        alertManager = AlertManager(application, cameraActivityDao, viewModelScope)
        openCvDetector = OpenCvDetector(application)
        
        applyPreferences()
        
        if (personDetectionEnabled) {
            openCvDetector?.initialize()
        }

        // Observe connection state to mark as paired
        webRTCManager.connectionState.observeForever { connected ->
            if (connected) {
                if (_isPaired.value == false) {
                    markAsPaired()
                }
                // When broadcaster connects, it becomes a creator
                if (webRTCManager.isBroadcaster) {
                    if (_isCreator.value == false) {
                        markAsCreator()
                    }
                }
            }
        }

        webRTCManager.onConnectionEvent = { sessionId, isHost, remoteDeviceId, connected ->
            handleWebRTCConnection(sessionId, isHost, remoteDeviceId, connected)
        }

        // Observe session status to log remote stops
        webRTCManager.sessionStatus.observeForever { status ->
            if (status == "closed") {
                val isBroadcaster = webRTCManager.isBroadcaster
                if (!isBroadcaster) {
                    // Monitor device observing that the remote camera has stopped
                    logActivity(
                        "Remote Camera Stopped",
                        "The camera has ended the session",
                        "monitor_disconnect",
                        "monitor"
                    )
                }
            }
        }
    }

    fun markAsPaired() {
        if (_isPaired.value == false) {
            _isPaired.value = true
            settingsPrefs.edit().putBoolean("is_paired", true).apply()
        }
    }

    fun markAsCreator() {
        if (_isCreator.value == false) {
            // Lazy generate Device ID only when becoming a creator
            var id = settingsPrefs.getString("my_device_id", null)
            if (id == null) {
                id = UUID.randomUUID().toString().replace("-", "").substring(0, 10)
                settingsPrefs.edit().putString("my_device_id", id).apply()
                myDeviceId.value = id
            } else {
                myDeviceId.value = id
            }

            _isCreator.value = true
            settingsPrefs.edit().putBoolean("is_creator", true).apply()
        }
    }

    fun generateHostSession() {
        _sessionSeconds.postValue(0)
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val newSessionId = (1..10).map { allowedChars.random() }.joinToString("")
        val newRoomId = UUID.randomUUID().toString().replace("-", "").substring(0, 12)
        val newHostId = myDeviceId.value ?: UUID.randomUUID().toString().substring(0, 8)

        sessionId.value = newSessionId
        roomId.value = newRoomId
        hostId.value = newHostId

        listenForViewers(newSessionId)

        // Create JSON for QR Code with timestamp for expiration check
        val qrJson = """
            {
                "device_id": "${myDeviceId.value}",
                "session_id": "$newSessionId",
                "room_id": "$newRoomId",
                "host_id": "$newHostId",
                "role": "HOST",
                "name": "${deviceName.value}",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        generateQrCode(qrJson)
        _isBroadcasting.value = true
        saveSessionToFirestore(newSessionId, newRoomId, newHostId)
    }

    private fun saveSessionToFirestore(sId: String, rId: String, hId: String) {
        val uid = auth.currentUser?.uid ?: return
        val sessionData = PersistentSession(
            sessionId = sId,
            roomId = rId,
            hostId = hId,
            connectionCode = sId,
            status = "active",
            updatedAt = System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .set(sessionData)
    }

    private fun loadDailyLimit() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastDate = limitPrefs.getString("last_date", "")

        if (today != lastDate) {
            _usedSecondsToday.postValue(0)
            limitPrefs.edit().putInt("used_seconds", 0).putString("last_date", today).apply()
        } else {
            _usedSecondsToday.postValue(limitPrefs.getInt("used_seconds", 0))
        }

        _dailyLimitSeconds.postValue(limitPrefs.getInt("daily_limit", 300))
    }

    fun incrementUsedSeconds() {
        val current = (_usedSecondsToday.value ?: 0) + 1
        _usedSecondsToday.postValue(current)

        val sess = (_sessionSeconds.value ?: 0) + 1
        _sessionSeconds.postValue(sess)

        if (current % 10 == 0) {
            limitPrefs.edit().putInt("used_seconds", current).apply()
        }
    }

    fun extendLimit(seconds: Int) {
        val newLimit = (_dailyLimitSeconds.value ?: 300) + seconds
        _dailyLimitSeconds.postValue(newLimit)
        limitPrefs.edit().putInt("daily_limit", newLimit).apply()
    }

    fun saveFinalUsedSeconds() {
        limitPrefs.edit().putInt("used_seconds", _usedSecondsToday.value ?: 0).apply()
    }

    fun updateDeviceName(newName: String) {
        deviceName.postValue(newName)
        settingsPrefs.edit().putString("my_device_name", newName).apply()
    }

    fun logActivity(title: String, subtitle: String, iconType: String, role: String = "camera") {
        val now = System.currentTimeMillis()
        // Debounce: Ignore identical logs within 3 seconds to prevent UI spam
        if (lastLoggedActivity?.first == title && lastLoggedActivity?.second == subtitle && (now - lastLogTimestamp) < 3000) {
            return
        }
        lastLoggedActivity = title to subtitle
        lastLogTimestamp = now

        val id = UUID.randomUUID().toString()
        val activity = CameraActivity(id, title, subtitle, now, iconType, role)
        viewModelScope.launch(Dispatchers.IO) {
            cameraActivityDao.insert(activity)
            Log.d(tag, "Activity logged to Room: $title ($role)")
        }
    }

    private fun observeLocalActivities() {
        viewModelScope.launch {
            cameraActivityDao.getAllActivities().collect { activities ->
                _cameraActivities.postValue(activities)
            }
        }
    }

    private fun observeLocalDevices() {
        viewModelScope.launch {
            pairedDeviceDao.getAllPairedDevices().collect { devices ->
                _savedDevices.postValue(devices)
            }
        }
        viewModelScope.launch {
            connectedViewerDao.getAllConnectedViewers().collect { viewers ->
                _savedViewers.postValue(viewers)
            }
        }
    }

    fun saveRoomDevice(device: Device) {
        val uid = auth.currentUser?.uid ?: return
        val docId = if (device.id.isNotEmpty()) device.id else UUID.randomUUID().toString()
        val finalDevice = if (device.id.isEmpty()) device.copy(id = docId) else device

        firestore.collection("users").document(uid)
            .collection("IPCameras").document(docId)
            .set(finalDevice)
    }

    fun deleteRoomDevice(device: Device) {
        val uid = auth.currentUser?.uid ?: return
        if (device.id.isEmpty()) return
        firestore.collection("users").document(uid)
            .collection("IPCameras").document(device.id)
            .delete()
    }

    private val _recordVideos = MutableLiveData<List<VideoRecord>>(emptyList())
    val recordVideos: LiveData<List<VideoRecord>> = _recordVideos

    private val _monitorVideos = MutableLiveData<List<VideoRecord>>(emptyList())
    val monitorVideos: LiveData<List<VideoRecord>> = _monitorVideos

    private val _ipCameraVideos = MutableLiveData<List<VideoRecord>>(emptyList())
    val ipCameraVideos: LiveData<List<VideoRecord>> = _ipCameraVideos

    private val _snapshots = MutableLiveData<List<VideoRecord>>(emptyList())
    val snapshots: LiveData<List<VideoRecord>> = _snapshots

    fun refreshVideoRecords(filterDate: Date? = null) {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            withContext(Dispatchers.IO) {
                val downloadDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Sentinel Video"
                )
                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Sentinel Captures"
                )

                // 1. Fetch Videos
                var videoFiles = if (downloadDir.exists()) {
                    downloadDir.listFiles { file ->
                        val extensions = listOf("mp4", "mkv", "3gp", "webm", "avi", "m4a")
                        extensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
                    }?.toList() ?: emptyList()
                } else emptyList()

                // 2. Fetch Snapshots (Images)
                var snapshotFiles = if (picturesDir.exists()) {
                    picturesDir.listFiles { file ->
                        val extensions = listOf("jpg", "jpeg", "png")
                        extensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
                    }?.toList() ?: emptyList()
                } else emptyList()

                if (filterDate != null) {
                    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    val filterDateStr = sdf.format(filterDate)
                    videoFiles = videoFiles.filter {
                        val fileDateStr = sdf.format(Date(it.lastModified()))
                        fileDateStr == filterDateStr
                    }
                    snapshotFiles = snapshotFiles.filter {
                        val fileDateStr = sdf.format(Date(it.lastModified()))
                        fileDateStr == filterDateStr
                    }
                }

                videoFiles = videoFiles.sortedByDescending { it.lastModified() }
                snapshotFiles = snapshotFiles.sortedByDescending { it.lastModified() }

                val allVideoRecords = videoFiles.map { file -> createVideoRecord(file) }
                val allSnapshotRecords = snapshotFiles.map { file -> createSnapshotRecord(file) }

                // Categorize
                // 1. Ip Camera: Starts with RTSP_
                val ipVideos = allVideoRecords.filter { it.name.startsWith("RTSP_") }

                // 2. Monitor Videos: Ends with .m4a or starts with MON_
                val monitorVids =
                    allVideoRecords.filter { it.file.extension == "m4a" || it.name.startsWith("MON_") }

                // 3. Record Videos (Phone): Starts with REC_ and is NOT a monitor video
                val phoneVids = allVideoRecords.filter {
                    it.name.startsWith("REC_") && it.file.extension != "m4a" && !it.name.startsWith(
                        "MON_"
                    )
                }

                _videoRecords.postValue(allVideoRecords)
                _recordVideos.postValue(phoneVids)
                _monitorVideos.postValue(monitorVids)
                _ipCameraVideos.postValue(ipVideos)
                _snapshots.postValue(allSnapshotRecords)
            }
            _isLoadingVideos.value = false
        }
    }

    private fun createSnapshotRecord(file: File): VideoRecord {
        val date = Date(file.lastModified())
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val formattedDate = formatter.format(date)

        var bitmap: Bitmap? = null
        try {
            bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
        }

        return VideoRecord(
            file = file,
            name = file.name,
            formattedDate = formattedDate,
            duration = "Image",
            thumbnail = bitmap,
            size = "${(file.length() / 1024)} KB"
        )
    }

    private fun createVideoRecord(file: File): VideoRecord {
        val date = Date(file.lastModified())
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val formattedDate = formatter.format(date)

        var durationStr = "0s"
        var thumbnail: Bitmap? = null
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            val durationMs = time?.toLong() ?: 0
            durationStr = "${(durationMs / 1000)}s"

            thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    file,
                    android.util.Size(120, 120),
                    null
                )
            } else {
                @Suppress("DEPRECATION")
                ThumbnailUtils.createVideoThumbnail(
                    file.absolutePath,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
            }
        } catch (e: Exception) {
            // Log.e(tag, "Error extracting metadata for ${file.name}: ${e.message}")
        }

        return VideoRecord(
            file = file,
            name = file.name,
            formattedDate = formattedDate,
            duration = durationStr,
            thumbnail = thumbnail,
            size = "${(file.length() / 1024)} KB"
        )
    }

    fun fetchAndSaveDeviceMetadata(sessionId: String, role: String = "camera") {
        val database = FirebaseDatabase.getInstance()
        database.getReference("sessions/$sessionId/metadata")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name =
                        snapshot.child("name").getValue(String::class.java) ?: "Unknown Device"
                    val deviceId = snapshot.child("deviceId").getValue(String::class.java)
                        ?: "unknown_$sessionId"
                    
                    // Update UI preview name
                    val currentNames = activePreviewDeviceNames.value ?: emptyMap()
                    activePreviewDeviceNames.postValue(currentNames + (sessionId to name))

                    // Log the addition of the device
                    if (role == "camera") {
                        // This device is a Monitor adding a remote Camera
                        logActivity("Remote Camera Linked", name, "monitor_connect", "monitor")
                    } else {
                        // This device is a Camera being linked to by a remote Monitor
                        logActivity("Monitor Linked", name, "monitor_connect", "camera")
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun handleWebRTCConnection(
        sessionId: String,
        isHost: Boolean,
        remoteDeviceId: String,
        connected: Boolean
    ) {
        if (connected) {
            syncDeviceData(sessionId, isHost, remoteDeviceId)
        } else {
            markDeviceDisconnected(isHost, remoteDeviceId)
        }
    }

    private fun syncDeviceData(sessionId: String, isHost: Boolean, remoteDeviceId: String) {
        val database = FirebaseDatabase.getInstance()
        val path = if (isHost) {
            "sessions/$sessionId/viewers/$remoteDeviceId"
        } else {
            "sessions/$sessionId/metadata"
        }

        database.getReference(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown Device"
                val model = snapshot.child("model").getValue(String::class.java)
                    ?: snapshot.child("name").getValue(String::class.java) ?: "Unknown Model"
                val status = snapshot.child("status").getValue(String::class.java) ?: "online"
                
                val rId = snapshot.child("roomId").getValue(String::class.java) ?: ""
                val hId = snapshot.child("hostId").getValue(String::class.java) ?: ""
                val pId = snapshot.child("peerId").getValue(String::class.java) ?: ""

                viewModelScope.launch {
                    if (isHost) {
                        val existing = connectedViewerDao.getById(remoteDeviceId)
                        val viewer = ConnectedViewer(
                            deviceId = remoteDeviceId,
                            sessionId = sessionId,
                            roomId = rId,
                            hostId = hId,
                            peerId = pId.ifEmpty { remoteDeviceId },
                            name = name,
                            model = model,
                            status = status,
                            connectionStatus = "Connected",
                            lastConnectedTime = System.currentTimeMillis(),
                            lastSeen = System.currentTimeMillis(),
                            timestamp = existing?.timestamp ?: System.currentTimeMillis()
                        )
                        connectedViewerDao.insert(viewer)
                        Log.d(tag, "Viewer connection synced: $name ($remoteDeviceId)")
                        
                        // Persistent persistence in Firestore
                        saveViewerToFirestore(remoteDeviceId, name, model)
                        saveLinkedDeviceToFirestore(remoteDeviceId, name)
                    } else {
                        val existing = pairedDeviceDao.getById(remoteDeviceId)
                        val device = PairedDevice(
                            deviceId = remoteDeviceId,
                            sessionId = sessionId,
                            roomId = rId,
                            hostId = hId,
                            peerId = pId.ifEmpty { remoteDeviceId },
                            name = name,
                            model = model,
                            status = status,
                            connectionStatus = "Connected",
                            lastConnectedTime = System.currentTimeMillis(),
                            lastSeen = System.currentTimeMillis(),
                            timestamp = existing?.timestamp ?: System.currentTimeMillis(),
                            role = "monitor"
                        )
                        pairedDeviceDao.insert(device)
                        Log.d(tag, "Monitor connection synced: $name ($remoteDeviceId)")
                        
                        // Persistent persistence in Firestore
                        savePairedDeviceToFirestore(remoteDeviceId, name, model)
                        saveLinkedDeviceToFirestore(remoteDeviceId, name)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveViewerToFirestore(viewerId: String, name: String, model: String) {
        val uid = auth.currentUser?.uid ?: return
        val viewerData = mapOf(
            "deviceId" to viewerId,
            "name" to name,
            "model" to model,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .collection("ConnectedViewers").document(viewerId)
            .set(viewerData)
    }

    private fun savePairedDeviceToFirestore(deviceId: String, name: String, model: String) {
        val uid = auth.currentUser?.uid ?: return
        val deviceData = mapOf(
            "deviceId" to deviceId,
            "name" to name,
            "model" to model,
            "timestamp" to System.currentTimeMillis(),
            "role" to "monitor"
        )
        firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .collection("ConnectedMonitors").document(deviceId)
            .set(deviceData)
    }

    private fun saveLinkedDeviceToFirestore(deviceId: String, name: String) {
        val uid = auth.currentUser?.uid ?: return
        val deviceData = mapOf(
            "deviceId" to deviceId,
            "name" to name,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("LinkedDevices").document(deviceId)
            .set(deviceData)
    }

    private fun markDeviceDisconnected(isHost: Boolean, remoteDeviceId: String) {
        viewModelScope.launch {
            if (isHost) {
                val existing = connectedViewerDao.getById(remoteDeviceId)
                if (existing != null) {
                    connectedViewerDao.insert(
                        existing.copy(
                            connectionStatus = "Disconnected",
                            lastSeen = System.currentTimeMillis()
                        )
                    )
                    Log.d(tag, "Viewer marked disconnected: ${existing.name}")
                }
            } else {
                val existing = pairedDeviceDao.getById(remoteDeviceId)
                if (existing != null) {
                    pairedDeviceDao.insert(
                        existing.copy(
                            connectionStatus = "Disconnected",
                            lastSeen = System.currentTimeMillis()
                        )
                    )
                    Log.d(tag, "Monitor marked disconnected: ${existing.name}")
                }
            }
        }
    }

    fun deleteSavedDevice(deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pairedDeviceDao.deleteById(deviceId)
        }
    }

    fun takeScreenshot(context: Context, isLocal: Boolean = true): String? {
        val path = webRTCManager.takeScreenshot(context, isLocal)
        if (path != null) {
            val role = if (isLocal) "camera" else "monitor"
            logActivity("Screenshot Taken", File(path).name, "screenshot", role)
        }
        return path
    }

    fun resumeHostSession(sid: String, rId: String = "", hId: String = "", targetViewerId: String? = null, isAudio: Boolean = false) {
        this.isAudio = isAudio
        
        val uid = auth.currentUser?.uid
        if (uid == null) {
            proceedWithResumption(sid, rId, hId, targetViewerId, isAudio)
            return
        }

        // Rule: Read existing session from Firestore
        firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .get()
            .addOnSuccessListener { doc ->
                val session = doc.toObject(PersistentSession::class.java)
                if (session != null && session.status == "active") {
                    // Proceed with resumption - we'll be more lenient with the viewer check
                    // to ensure reconnection is attemptable if it exists in local Saved Viewers
                    proceedWithResumption(
                        session.sessionId,
                        session.roomId,
                        session.hostId,
                        targetViewerId,
                        isAudio
                    )
                } else {
                    // Fallback to provided IDs if Firestore session is missing but IDs were passed
                    if (sid.isNotEmpty()) {
                        proceedWithResumption(sid, rId, hId, targetViewerId, isAudio)
                    } else {
                        Log.e(tag, "Reconnect ignored: No active session found")
                    }
                }
            }
            .addOnFailureListener {
                if (sid.isNotEmpty()) proceedWithResumption(sid, rId, hId, targetViewerId, isAudio)
            }
    }

    private fun proceedWithResumption(sid: String, rId: String, hId: String, targetViewerId: String?, isAudio: Boolean) {
        Log.d(tag, "Proceeding with session resumption: $sid, viewer: $targetViewerId")
        sessionId.value = sid
        roomId.value = rId.ifEmpty { sid }
        hostId.value = hId.ifEmpty { myDeviceId.value ?: "" }

        listenForViewers(sid)
        _isBroadcasting.value = true
        
        // Ensure metadata is fresh in Firebase before monitor tries to connect
        val name = deviceName.value ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        val dId = myDeviceId.value ?: ""
        val metadata = mapOf(
            "name" to name,
            "deviceId" to dId,
            "sessionId" to sid,
            "model" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "status" to "online",
            "type" to "Camera",
            "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        FirebaseDatabase.getInstance().getReference("sessions/$sid/metadata").setValue(metadata)

        // Start streaming locally so we are "online"
        startStreaming(null, isAudio)
        
        // Reconnect requests should ONLY be sent to a DIFFERENT device.
        if (targetViewerId != null && targetViewerId != myDeviceId.value) {
            Log.d(tag, "Sending reconnect request to viewer: $targetViewerId")
            // Clean up existing local WebRTC state for this viewer to allow a fresh connection
            webRTCManager.resetViewerConnection(sid, targetViewerId)

            // Clear signaling data for this viewer in Firebase to ensure a fresh connection
            FirebaseDatabase.getInstance().getReference("sessions/$sid/viewers/$targetViewerId").removeValue()
                .addOnCompleteListener {
                    // Small delay to ensure "streaming" state is propagated
                    Handler(Looper.getMainLooper()).postDelayed({
                        sendFirestoreReconnectRequest(sid, roomId.value ?: sid, hostId.value ?: "", targetViewerId)
                    }, 800)
                }
        }
    }

    val requestDeclinedMessage = MutableLiveData<String?>(null)
    private var sentRequestStatusListener: ListenerRegistration? = null

    private fun sendFirestoreReconnectRequest(sId: String, rId: String, hId: String, viewerDeviceId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e(tag, "sendFirestoreReconnectRequest failed: User is not logged in")
            return
        }

        val requestId = UUID.randomUUID().toString()
        Log.d(tag, "Creating Firestore reconnect request: $requestId for viewer: $viewerDeviceId (Host UID: $uid)")
        
        val request = ReconnectRequest(
            requestId = requestId,
            hostId = hId.ifEmpty { myDeviceId.value ?: "" },
            viewerId = viewerDeviceId,
            roomId = rId.ifEmpty { sId },
            sessionId = sId,
            hostName = deviceName.value ?: "NannyEye Monitor",
            status = "pending",
            createdAt = System.currentTimeMillis()
        )

        val requestDoc = firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .collection("ReconnectRequests").document(requestId)

        requestDoc.set(request)
            .addOnSuccessListener {
                Log.d(tag, "Firestore reconnect request sent successfully")
                // Listen for status changes (accepted/declined)
                listenForRequestResponse(requestDoc, viewerDeviceId)
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to send Firestore reconnect request: ${e.message}")
            }
    }

    private fun listenForRequestResponse(docRef: com.google.firebase.firestore.DocumentReference, viewerDeviceId: String) {
        sentRequestStatusListener?.remove()
        sentRequestStatusListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            
            val status = snapshot.getString("status") ?: "pending"
            val viewerName = _savedViewers.value?.find { it.deviceId == viewerDeviceId }?.name ?: "Monitor"
            
            if (status == "declined") {
                Log.d(tag, "Reconnect request declined by $viewerName")
                requestDeclinedMessage.postValue("$viewerName declined your request")
                
                // Stop streaming as the intended viewer declined
                stopStreaming()
                
                // Clean up listener
                sentRequestStatusListener?.remove()
                sentRequestStatusListener = null
            } else if (status == "accepted") {
                Log.d(tag, "Reconnect request accepted by $viewerName")
                // Success - streaming continues as viewer will connect via WebRTC
                sentRequestStatusListener?.remove()
                sentRequestStatusListener = null
            }
        }
    }
    
    val incomingReconnectRequest = MutableLiveData<Map<String, String>?>(null)

    /**
     * Listens for incoming reconnection requests.
     * Note: Uses collectionGroup to find requests sent by ANY host to THIS viewer's device ID.
     * IMPORTANT: This requires a 'collectionGroup' index in the Firebase Console for 'ReconnectRequests'.
     * If the index is missing, check Logcat for a link to generate it.
     */
    private var reconnectRequestsListener: ListenerRegistration? = null

    private fun listenForFirestoreReconnectRequests() {
        val dId = myDeviceId.value ?: return

        reconnectRequestsListener?.remove()

        // We use collectionGroup because the request might be in the Host's UID path,
        // not the Viewer's UID path.
        Log.d(tag, "Listening for ALL ReconnectRequests where viewerId is: $dId")

        reconnectRequestsListener = firestore.collectionGroup("ReconnectRequests")
            .whereEqualTo("viewerId", dId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(tag, "Error listening for ReconnectRequests: ${e.message}")
                    return@addSnapshotListener
                }

                val requests = snapshots?.toObjects(ReconnectRequest::class.java) ?: emptyList()
                Log.d(tag, "ReconnectRequests (collectionGroup) received. Count: ${requests.size}")

                if (requests.isNotEmpty()) {
                    // Get the most recent request
                    val req = requests.sortedByDescending { it.createdAt }.first()
                    val doc = snapshots!!.documents.find { it.get("requestId") == req.requestId || it.id == req.requestId }

                    if (doc != null) {
                        Log.d(tag, "Request found! Host: ${req.hostName}, Path: ${doc.reference.path}")
                        val map = mapOf(
                            "sessionId" to req.sessionId,
                            "roomId" to req.roomId,
                            "hostId" to req.hostId,
                            "hostName" to req.hostName,
                            "requestId" to req.requestId,
                            "docPath" to doc.reference.path
                        )
                        incomingReconnectRequest.postValue(map)
                    }
                } else {
                    incomingReconnectRequest.postValue(null)
                }
            }
    }

    fun acceptFirestoreReconnectRequest(requestId: String) {
        val path = incomingReconnectRequest.value?.get("docPath")
        if (path != null) {
            // Update the actual document at its host location
            firestore.document(path).update("status", "accepted")
                .addOnSuccessListener { incomingReconnectRequest.postValue(null) }
        } else {
            // Fallback if path is missing
            firestore.collectionGroup("ReconnectRequests")
                .whereEqualTo("requestId", requestId)
                .get()
                .addOnSuccessListener { snapshots ->
                    for (doc in snapshots) {
                        doc.reference.update("status", "accepted")
                    }
                    incomingReconnectRequest.postValue(null)
                }
        }
    }

    fun declineFirestoreReconnectRequest(requestId: String) {
        val path = incomingReconnectRequest.value?.get("docPath")
        if (path != null) {
            Log.d(tag, "Declining reconnect request at path: $path")
            firestore.document(path).update("status", "declined")
                .addOnSuccessListener { incomingReconnectRequest.postValue(null) }
        } else {
            val uid = auth.currentUser?.uid ?: return
            firestore.collection("users").document(uid)
                .collection("Session").document("active_session")
                .collection("ReconnectRequests").document(requestId)
                .update("status", "declined")
                .addOnSuccessListener { incomingReconnectRequest.postValue(null) }
        }
    }

    fun declineReconnect(viewerDeviceId: String? = null) {
        // Kept for backward compatibility if needed, but we should use declineFirestoreReconnectRequest
        incomingReconnectRequest.value?.get("requestId")?.let {
            declineFirestoreReconnectRequest(it)
        }
    }

    private val activeViewerIds = mutableSetOf<String>()

    fun listenForViewers(sid: String) {
        FirebaseDatabase.getInstance().getReference("sessions/$sid/viewers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val viewers = mutableListOf<ViewerInfo>()
                    val newIds = mutableSetOf<String>()

                    for (child in snapshot.children) {
                        val status = child.child("status").getValue(String::class.java) ?: "Online"
                        if (status == "Online") {
                            val dId =
                                child.child("deviceId").getValue(String::class.java) ?: child.key
                                ?: ""
                            val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                            viewers.add(ViewerInfo(dId, name, status))
                            newIds.add(dId)

                            val logKey = "${sid}_${dId}"
                            if (!loggedViewerSessions.contains(logKey)) {
                                logActivity("Monitor Connected", name, "monitor_connect", "camera")
                                loggedViewerSessions.add(logKey)
                                activeViewerIds.add(dId)
                            }
                        }
                    }

                    // Log disconnections based on activeViewerIds set to prevent duplicate logs from stale LiveData
                    val iterator = activeViewerIds.iterator()
                    while (iterator.hasNext()) {
                        val oldId = iterator.next()
                        if (!newIds.contains(oldId)) {
                            // Find name from previous state or default
                            val oldName =
                                _connectedViewers.value?.find { it.deviceId == oldId }?.name
                                    ?: "Unknown Monitor"
                            logActivity(
                                "Monitor Disconnected",
                                oldName,
                                "monitor_disconnect",
                                "camera"
                            )
                            loggedViewerSessions.remove("${sid}_${oldId}")
                            iterator.remove()
                        }
                    }

                    _connectedViewers.postValue(viewers)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun generateQrCode(content: String) {
        Thread {
            try {
                val hints = HashMap<EncodeHintType, Any>()
                hints[EncodeHintType.MARGIN] = 1

                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)

                val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

                for (x in 0 until 512) {
                    for (y in 0 until 512) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }

                qrBitmap.postValue(bmp)
            } catch (e: Exception) {
                Log.e(tag, "QR error: ${e.message}")
            }
        }.start()
    }

    private var motionResetHandler: android.os.Handler? = null
    private var motionResetRunnable: Runnable? = null

    fun createMotionSink(role: String = "camera"): MotionDetectionSink {
        return MotionDetectionSink(
            onMotionDetected = { percentage, frameData ->
                // Basic check to skip processing if both are disabled
                if (!motionEnabled && !personDetectionEnabled) return@MotionDetectionSink

                val devName = resolveDeviceName()

                if (personDetectionEnabled && frameData != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val bitmap = MotionDetector.toBitmap(
                                frameData.yPlane, frameData.uPlane, frameData.vPlane,
                                frameData.width, frameData.height, frameData.strideY
                            )
                            if (bitmap != null) {
                                // 1. ML Kit Detection (People, Pets, Vehicles)
                                val mlResult = mlKitDetector.detect(bitmap)
                                var reportedAi = false
                                
                                // 2. Handle Pets and Vehicles
                                if (mlResult.hasPet) {
                                    alertManager.logPetDetected(role, devName)
                                    reportedAi = true
                                }
                                if (mlResult.hasVehicle) {
                                    alertManager.logVehicleDetected(role, devName)
                                    reportedAi = true
                                }

                                // 3. Handle People Tracking
                                val personBoxes = if (mlResult.personBoxes.isNotEmpty()) {
                                    mlResult.personBoxes.map { BBox(it.left, it.top, it.width(), it.height()) }
                                } else if (!mlResult.hasPet && !mlResult.hasVehicle && openCvDetector?.isAvailable() == true) {
                                    // Fallback to OpenCV only if NO Pet or Vehicle was found by ML Kit
                                    openCvDetector?.detect(bitmap)?.personBoxes?.map { 
                                        BBox(it.x, it.y, it.width, it.height) 
                                    } ?: emptyList()
                                } else {
                                    emptyList()
                                }

                                val trackerResult = personTracker.update(personBoxes)
                                
                                // Update WebRTC State for Person
                                webRTCManager.setPersonDetected(trackerResult.hasPerson)

                                val now = System.currentTimeMillis()
                                if (trackerResult.hasPerson) {
                                    reportedAi = true
                                    // Handle New Person Notifications
                                    for (track in trackerResult.newTracks) {
                                        if (!track.personNotificationSent) {
                                            alertManager.logPersonDetected(role, deviceName = devName, personId = track.id)
                                            track.personNotificationSent = true
                                            track.lastMotionNotificationTime = now
                                        }
                                    }
                                    // Handle Motion for existing people
                                    val movingPersonIds = mutableListOf<Int>()
                                    for (track in trackerResult.matchedTracks) {
                                        if (now - track.lastMotionNotificationTime > 20_000L) {
                                            movingPersonIds.add(track.id)
                                            track.lastMotionNotificationTime = now
                                        }
                                    }
                                    if (movingPersonIds.isNotEmpty()) {
                                        alertManager.logPeopleMotion(movingPersonIds, role, devName)
                                    }
                                }

                                // UI update: if either AI reported or general motion is enabled
                                if (reportedAi || motionEnabled) {
                                    webRTCManager.setMotionDetected(true, percentage)
                                    scheduleMotionReset()
                                }
                                
                                // Notification logic for general motion:
                                // Always log motion if enabled, even if AI also reported something (matching "Both" request)
                                if (motionEnabled) {
                                    alertManager.logMotion(percentage, role, devName)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Detection error: ${e.message}")
                        }
                    }
                } else if (motionEnabled) {
                    // AI disabled, but general motion is enabled
                    webRTCManager.setMotionDetected(true, percentage)
                    webRTCManager.setPersonDetected(false)
                    scheduleMotionReset()
                    alertManager.logMotion(percentage, role, devName)
                }
            },
            intervalMs = 800L
        )
    }

    private fun resolveDeviceName(): String {
        val name = deviceName.value
        return if (name.isNullOrBlank()) "Camera" else name
    }

    private fun scheduleMotionReset() {
        val handler = motionResetHandler ?: run {
            android.os.Handler(android.os.Looper.getMainLooper()).also { motionResetHandler = it }
        }
        motionResetRunnable?.let { handler.removeCallbacks(it) }
        val r = Runnable {
            webRTCManager.setMotionDetected(false, 0f)
            webRTCManager.setPersonDetected(false)
        }
        motionResetRunnable = r
        handler.postDelayed(r, 3000L)
    }

    fun applyPreferences() {
        motionEnabled = appPrefs.getBoolean("motion_detection", false)
        personDetectionEnabled = appPrefs.getBoolean("ai_detection", false)
        val sensitivity = appPrefs.getFloat("motion_sensitivity", 0.5f)
        
        val isAnyDetectionEnabled = motionEnabled || personDetectionEnabled
        
        localMotionSink?.setDetecting(isAnyDetectionEnabled)
        localMotionSink?.setSensitivity(sensitivity)
        
        remoteMotionSink?.setDetecting(isAnyDetectionEnabled)
        remoteMotionSink?.setSensitivity(sensitivity)

        // Reset LiveData states if disabled
        if (!isAnyDetectionEnabled) {
            webRTCManager.setMotionDetected(false, 0f)
            webRTCManager.setPersonDetected(false)
            motionResetRunnable?.let { motionResetHandler?.removeCallbacks(it) }
        } else if (!personDetectionEnabled) {
            webRTCManager.setPersonDetected(false)
        }

        if (personDetectionEnabled && openCvDetector?.isAvailable() == false) {
            openCvDetector?.initialize()
        }
        
        Log.d(tag, "Preferences applied: motion=$motionEnabled, ai=$personDetectionEnabled, sensitivity=$sensitivity")
    }

    private fun setupLocalMotionDetection(role: String = "camera") {
        localMotionSink?.let { webRTCManager.removeLocalMotionSink() }
        val sink = createMotionSink(role)
        localMotionSink = sink
        
        // Apply current preferences to the new sink
        sink.setDetecting(motionEnabled)
        sink.setSensitivity(appPrefs.getFloat("motion_sensitivity", 0.5f))
        
        webRTCManager.addLocalMotionSink(sink)
    }

    private fun setupRemoteMotionDetection(role: String = "monitor") {
        remoteMotionSink?.let { webRTCManager.removeAllRemoteMotionSinks() }
        val sink = createMotionSink(role)
        remoteMotionSink = sink
        
        // Apply current preferences to the new sink
        sink.setDetecting(motionEnabled)
        sink.setSensitivity(appPrefs.getFloat("motion_sensitivity", 0.5f))

        activePreviewSessionIds.value?.forEach { sid ->
            webRTCManager.addRemoteMotionSink(sid, sink)
        }
    }

    fun startStreaming(localSink: VideoSink?, isAudio: Boolean, sid: String? = null) {
        this.isAudio = isAudio
        // If sid is "new" or we want a fresh start, clear the existing session
        if (sid == "new") {
            sessionId.value = ""
        } else if (!sid.isNullOrEmpty() && sessionId.value != sid) {
            resumeHostSession(sid, isAudio = isAudio)
            localSink?.let { webRTCManager.addLocalSink(it) }
            return
        }

        // Always generate a fresh session if one isn't currently active
        // this ensures every new entry into CameraViewScreen is a "new connection"
        if (sessionId.value.isNullOrEmpty()) {
            generateHostSession()
        }

        val sessionToUse = sessionId.value ?: return
        val name = deviceName.value ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        val dId = myDeviceId.value ?: ""

        logActivity("Camera Started", name, "camera_start", "camera")
        _isBroadcasting.postValue(true)

        webRTCManager.startCameraAndOffer(
            sessionToUse,
            dId,
            localSink,
            isAudio,
            name,
            object : WebRTCManager.OfferCallback {
                override fun onOfferCreated(sdp: String) {
                    Log.d(tag, "Offer created for session: $sessionToUse")
                }
            })

        Log.e("check8521", "Streaming start")
        if (motionEnabled) setupLocalMotionDetection("camera")
    }

    fun initRenderer(renderer: SurfaceViewRenderer, isLocal: Boolean = true) {
        webRTCManager.initSurfaceViewRenderer(renderer, isLocal)
        Log.d(tag, "Renderer initialized, isLocal: $isLocal")
    }

    fun startViewing(sessionId: String, remoteSink: VideoSink? = null) {
        _sessionSeconds.postValue(0)
        
        val currentSessions = activePreviewSessionIds.value ?: emptySet()
        activePreviewSessionIds.postValue(currentSessions + sessionId)
        
        val dId = myDeviceId.value ?: ""
        val name = deviceName.value ?: "${Build.MANUFACTURER} ${Build.MODEL}"

        // Register viewer in Firebase
        val viewerRef = FirebaseDatabase.getInstance()
            .getReference("sessions")
            .child(sessionId)
            .child("viewers")
            .child(dId)

        val viewerData = mapOf(
            "name" to name,
            "deviceId" to dId,
            "status" to "Online",
            "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
        )

        viewerRef.setValue(viewerData)
        viewerRef.onDisconnect().removeValue()

        logActivity("Viewing Started", "Connected to remote camera", "monitor_connect", "monitor")

        // When acting as a viewer, we are not necessarily broadcasting our own camera
        // so we don't set _isBroadcasting here.

        webRTCManager.connectAsViewer(sessionId, remoteSink, dId)
        if (motionEnabled) setupRemoteMotionDetection("monitor")
    }

    fun startRemotePreview(sessionId: String, sink: VideoSink) {
        val dId = myDeviceId.value ?: ""
        val currentSessions = activePreviewSessionIds.value ?: emptySet()
        activePreviewSessionIds.postValue(currentSessions + sessionId)
        webRTCManager.connectAsViewer(sessionId, sink, dId)
        if (motionEnabled && remoteMotionSink == null) {
            setupRemoteMotionDetection("monitor")
        } else if (motionEnabled && remoteMotionSink != null) {
            webRTCManager.addRemoteMotionSink(sessionId, remoteMotionSink!!)
        }
    }

    fun stopRemotePreview(sessionId: String? = null) {
        if (sessionId != null) {
            webRTCManager.stopViewer(sessionId)
            val currentSessions = activePreviewSessionIds.value ?: emptySet()
            activePreviewSessionIds.postValue(currentSessions - sessionId)
            
            val currentNames = activePreviewDeviceNames.value ?: emptyMap()
            activePreviewDeviceNames.postValue(currentNames - sessionId)
        }
    }

    fun removeRemoteSink(sink: VideoSink) {
        webRTCManager.removeRemoteSink(sink)
    }

    fun removeLocalSink(sink: VideoSink) {
        webRTCManager.removeLocalSink(sink)
    }

    fun stopViewing(sink: VideoSink? = null) {
        motionResetRunnable?.let { motionResetHandler?.removeCallbacks(it) }
        sink?.let { webRTCManager.removeRemoteSink(it) }
        webRTCManager.removeAllRemoteMotionSinks()
        remoteMotionSink = null
        personTracker.reset()

        val name = deviceName.value ?: "This device"
        logActivity("Viewing Stopped", "Disconnected from camera", "monitor_disconnect", "monitor")

        activePreviewSessionIds.value?.forEach { sid ->
            webRTCManager.stopViewer(sid)
        }
        activePreviewSessionIds.postValue(emptySet())
        activePreviewDeviceNames.postValue(emptyMap())
    }

    fun stopStreaming(sink: VideoSink? = null) {
        motionResetRunnable?.let { motionResetHandler?.removeCallbacks(it) }
        sink?.let { webRTCManager.removeLocalSink(it) }
        webRTCManager.removeLocalMotionSink()
        localMotionSink = null
        personTracker.reset()
        val name = deviceName.value ?: "This device"
        logActivity("Camera Stopped", name, "camera_stop", "camera")

        sessionId.value?.let { sid ->
            webRTCManager.stopHost(sid)
        }
        sessionId.postValue("")
        _isBroadcasting.postValue(false)
    }

    fun stop() {
        motionResetRunnable?.let { motionResetHandler?.removeCallbacks(it) }
        motionResetHandler?.removeCallbacksAndMessages(null)
        webRTCManager.removeLocalMotionSink()
        localMotionSink = null
        webRTCManager.removeAllRemoteMotionSinks()
        remoteMotionSink = null
        personTracker.reset()

        sessionId.value?.let { sid ->
            if (sid.isNotEmpty()) {
                val did = myDeviceId.value
                if (did != null) {
                    FirebaseDatabase.getInstance().getReference("SaveSessions")
                        .child(did)
                        .child(sid)
                        .removeValue()
                }
                webRTCManager.stopHost(sid)
            }
        }

        activePreviewSessionIds.value?.forEach { sid ->
            webRTCManager.stopViewer(sid)
        }
        activePreviewSessionIds.postValue(emptySet())
        activePreviewDeviceNames.postValue(emptyMap())

        _isBroadcasting.postValue(false)
    }

    fun switchCamera() {
        webRTCManager.switchCamera()
        webRTCManager.setMotionDetected(false, 0f)
        webRTCManager.setPersonDetected(false)
    }

    fun startRecording(context: Context, audioOnly: Boolean = false) {
        if (_isRecording.value == true) return

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "RECORD_AUDIO permission not granted")
            return
        }

        val outputDir = getDownloadDirectory()
        try {
            val rm = RecorderManager(webRTCManager)
            rm.startRecording(outputDir, audioOnly)
            recorderManager = rm
            _isRecording.postValue(true)
            Log.d(tag, "Recording started to: $outputDir")
        } catch (e: Exception) {
            Log.e(tag, "startRecording error: ${e.message}")
            recorderManager = null
            _isRecording.postValue(false)
        }
    }

    fun stopRecording(): String? {
        val rm = recorderManager ?: return null
        recorderManager = null
        _isRecording.postValue(false)
        var path: String? = null
        try {
            path = rm.stopRecording()
            Log.d(tag, "Recording stopped, saved to: $path")

            val fileName = path?.let { File(it).name } ?: "Video clip"
            val role = if (webRTCManager.isBroadcaster) "camera" else "monitor"
            logActivity("Video recorded", fileName, "video_record", role)
        } catch (e: Exception) {
            Log.e(tag, "stopRecording error: ${e.message}")
        }
        return path
    }

    private fun getDownloadDirectory(): File {
        val app = getApplication<Application>()
        if (isAudio) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sm = app.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val volumes = sm.storageVolumes
                if (volumes.isNotEmpty()) {
                    // Using reflection as getDirectory() might be hidden in some SDK versions
                    try {
                        val getDirMethod = volumes[0].javaClass.getMethod("getDirectory")
                        val dir = getDirMethod.invoke(volumes[0]) as? File
                        if (dir != null) return File(dir, "Download/Sentinel Video/Audio")
                    } catch (e: Exception) {
                        Log.e(tag, "StorageVolume.getDirectory failed: ${e.message}")
                    }
                }
            }
            return File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Sentinel Video/Audio"
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sm = app.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val volumes = sm.storageVolumes
                if (volumes.isNotEmpty()) {
                    try {
                        val getDirMethod = volumes[0].javaClass.getMethod("getDirectory")
                        val dir = getDirMethod.invoke(volumes[0]) as? File
                        if (dir != null) return File(dir, "Download/Sentinel Video")
                    } catch (e: Exception) {
                        Log.e(tag, "StorageVolume.getDirectory failed: ${e.message}")
                    }
                }
            }
            return File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Sentinel Video"
            )
        }
    }

    fun stopAll() {
        val sid = sessionId.value
        val did = viewerDeviceId.value
        if (sid != null && did != null) {
            FirebaseDatabase.getInstance().getReference("SaveSessions")
                .child(did)
                .child(sid)
                .removeValue()
        }
        stopViewing()
        stopStreaming()

        sessionId.postValue("")
        _sessionSeconds.postValue(0)
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        webRTCManager.setMicrophoneEnabled(enabled)
    }

    fun declineRemoteSession(sessionId: String, broadcasterDeviceId: String? = null) {
        if (sessionId.isEmpty()) return
        val database = FirebaseDatabase.getInstance()
        database.getReference("sessions").child(sessionId).child("status").setValue("declined")

        // Remove from SaveSessions if we know which device it belongs to
        if (broadcasterDeviceId != null) {
            database.getReference("SaveSessions").child(broadcasterDeviceId).child(sessionId)
                .removeValue()
        }

        // Reset status after a short delay so it doesn't stay declined forever
        Handler(Looper.getMainLooper()).postDelayed({
            database.getReference("sessions").child(sessionId).child("status").removeValue()
        }, 2000)
    }

    fun getSessionStatus(sessionId: String): LiveData<String?> {
        return webRTCManager.getSessionStatus(sessionId)
    }

    fun getSessionConnectionState(sessionId: String): LiveData<Boolean> {
        return webRTCManager.getSessionConnectionState(sessionId)
    }

    private fun setupFirestoreListeners() {
        val uid = auth.currentUser?.uid ?: return
        ipCamerasListener = firestore.collection("users").document(uid)
            .collection("IPCameras")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Firestore listen error: ${error.message}")
                    return@addSnapshotListener
                }
                val devices = snapshot?.toObjects(Device::class.java) ?: emptyList()
                _roomDevices.postValue(devices)
            }
    }

    override fun onCleared() {
        super.onCleared()
        ipCamerasListener?.remove()
        reconnectRequestsListener?.remove()
        sentRequestStatusListener?.remove()
        stopAll()
        webRTCManager.release()
    }
}
