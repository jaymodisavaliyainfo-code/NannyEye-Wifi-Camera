package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LinkedDevicesViewModel(application: Application) : AndroidViewModel(application), DefaultLifecycleObserver {
    private val repository = LinkedDevicesRepository()
    
    private val _devices = MutableStateFlow<List<LinkedDevice>>(emptyList())
    val devices: StateFlow<List<LinkedDevice>> = _devices

    val currentDeviceId: String = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    private var devicesJob: kotlinx.coroutines.Job? = null
    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private var remoteLogoutRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var deviceRegisteredInSession = false

    private val authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            deviceRegisteredInSession = false
            startObserving()
            registerCurrentDevice()
            listenForRemoteLogout()
        } else {
            deviceRegisteredInSession = false
            _devices.value = emptyList()
            devicesJob?.cancel()
            remoteLogoutRegistration?.remove()
        }
    }

    init {
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        updateActivity()
        startHeartbeat()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopHeartbeat()
        viewModelScope.launch {
            try {
                repository.setOffline(currentDeviceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000) // Update status every 30 seconds
                updateActivity()
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override fun onCleared() {
        super.onCleared()
        com.google.firebase.auth.FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        remoteLogoutRegistration?.remove()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    private fun listenForRemoteLogout() {
        remoteLogoutRegistration?.remove()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val id = user?.uid
        if (id.isNullOrEmpty()) return
        
        remoteLogoutRegistration = firestore.collection("users").document(id).collection("LinkedDevices")
            .document(currentDeviceId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && auth.currentUser != null) {
                    if (snapshot.exists()) {
                        deviceRegisteredInSession = true
                    } else if (deviceRegisteredInSession) {
                        // Only force logout if it existed in this session and then was deleted
                        auth.signOut()
                    }
                }
            }
    }

    private fun startObserving() {
        devicesJob?.cancel()
        devicesJob = viewModelScope.launch {
            repository.getLinkedDevices().collect {
                _devices.value = it.map { device ->
                    device.copy(isCurrent = device.deviceId == currentDeviceId)
                }
            }
        }
    }

    private fun observeDevices() {
        // Kept for backward compatibility if called elsewhere, but we use startObserving() now
        startObserving()
    }

    private fun registerCurrentDevice() {
        viewModelScope.launch {
            try {
                val device = LinkedDevice(
                    deviceId = currentDeviceId,
                    deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                    deviceModel = android.os.Build.MODEL,
                    platform = "Android",
                    loginTime = Timestamp.now(),
                    lastActive = Timestamp.now(),
                    status = "Online"
                )
                repository.registerDevice(device)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateActivity() {
        viewModelScope.launch {
            try {
                repository.updateLastActive(currentDeviceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logoutDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                repository.logoutDevice(deviceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logoutAllExceptCurrent() {
        viewModelScope.launch {
            try {
                repository.logoutAllExcept(currentDeviceId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*fun logoutAllAndExit(navController: NavController) {
        viewModelScope.launch {
            try {
                repository.logoutAll()
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/
}
