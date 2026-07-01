package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink

class MonitorWallViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "MonitorWallVM"
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val webRTCManager = WebRTCManager(application)

    private val _connectedMonitors = MutableStateFlow<List<PairedDevice>>(emptyList())
    val connectedMonitors: StateFlow<List<PairedDevice>> = _connectedMonitors

    private val _onlineMonitors = MutableLiveData<Map<String, CameraViewModel.MonitorInfo>>(emptyMap())
    val onlineMonitors: LiveData<Map<String, CameraViewModel.MonitorInfo>> = _onlineMonitors

    private val _gridSize = MutableStateFlow(4) // Default 2x2
    val gridSize: StateFlow<Int> = _gridSize

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterOnlineOnly = MutableStateFlow(false)
    val filterOnlineOnly: StateFlow<Boolean> = _filterOnlineOnly

    private val _sortBy = MutableStateFlow("Name") // "Name" or "Last Connected"
    val sortBy: StateFlow<String> = _sortBy

    val myDeviceId: String = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    init {
        listenToConnectedMonitors()
        listenToOnlineMonitors()
    }

    private fun listenToConnectedMonitors() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("Session").document("active_session")
            .collection("ConnectedMonitors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }
                val devices = snapshot?.toObjects(PairedDevice::class.java) ?: emptyList()
                _connectedMonitors.value = devices.filter { it.role == "monitor" }
            }
    }

    private fun listenToOnlineMonitors() {
        database.getReference("monitors")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val map = mutableMapOf<String, CameraViewModel.MonitorInfo>()
                    for (child in snapshot.children) {
                        val mId = child.key ?: continue
                        val sId = child.child("activeSessionId").getValue(String::class.java) ?: continue
                        val occupied = child.child("isOccupied").getValue(Boolean::class.java) ?: false
                        map[mId] = CameraViewModel.MonitorInfo(sId, occupied)
                    }
                    _onlineMonitors.postValue(map)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }

    fun setGridSize(size: Int) {
        _gridSize.value = size
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFilterOnlineOnly() {
        _filterOnlineOnly.value = !_filterOnlineOnly.value
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun connectCamera(sessionId: String, sink: VideoSink) {
        webRTCManager.connectAsViewer(sessionId, sink, myDeviceId)
    }

    fun disconnectCamera(sessionId: String) {
        // We don't stop the viewer session here to allow concurrent background viewing
        // and seamless navigation between screens.
        // webRTCManager.stopViewer(sessionId)
    }

    fun initRenderer(renderer: SurfaceViewRenderer) {
        webRTCManager.initSurfaceViewRenderer(renderer, isLocal = false)
    }

    fun getSessionConnectionState(sessionId: String): LiveData<Boolean> {
        return webRTCManager.getSessionConnectionState(sessionId)
    }

    override fun onCleared() {
        super.onCleared()
        // We rely on the shared WebRTCManager lifecycle managed by CameraViewModel
        // to avoid interrupting concurrent sessions when navigating away from the wall.
        /*_connectedMonitors.value.forEach {
            if (it.sessionId.isNotEmpty()) {
                webRTCManager.stopViewer(it.sessionId)
            }
        }*/
    }
}
