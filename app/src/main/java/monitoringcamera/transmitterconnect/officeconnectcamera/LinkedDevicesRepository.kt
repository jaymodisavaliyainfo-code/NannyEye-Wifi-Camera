package monitoringcamera.transmitterconnect.officeconnectcamera

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LinkedDevicesRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getDevicesCollection() = auth.currentUser?.let { user ->
        val id = user.uid
        if (id.isEmpty()) return@let null
        firestore.collection("users").document(id).collection("LinkedDevices")
    }

    suspend fun registerDevice(device: LinkedDevice) {
        getDevicesCollection()?.document(device.deviceId)?.set(device)?.await()
    }

    suspend fun updateLastActive(deviceId: String) {
        val data = mapOf(
            "lastActive" to Timestamp.now(),
            "status" to "Online"
        )
        getDevicesCollection()?.document(deviceId)?.set(data, SetOptions.merge())?.await()
    }

    suspend fun setOffline(deviceId: String) {
        val data = mapOf("status" to "Offline")
        getDevicesCollection()?.document(deviceId)?.set(data, SetOptions.merge())?.await()
    }

    fun getLinkedDevices(): Flow<List<LinkedDevice>> = callbackFlow {
        val collection = getDevicesCollection()
        if (collection == null) {
            trySend(emptyList())
        }
        val subscription = collection?.orderBy("lastActive", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val devices = snapshot?.toObjects(LinkedDevice::class.java) ?: emptyList()
                trySend(devices)
            }
        awaitClose { subscription?.remove() }
    }

    suspend fun logoutDevice(deviceId: String) {
        getDevicesCollection()?.document(deviceId)?.delete()?.await()
        // Note: Real remote logout would involve Firebase Cloud Messaging or a shared state in Firestore
        // that the other device listens to and then calls auth.signOut() locally.
    }

    suspend fun logoutAllExcept(currentDeviceId: String) {
        val collection = getDevicesCollection() ?: return
        val snapshot = collection.get().await()
        for (doc in snapshot.documents) {
            if (doc.id != currentDeviceId) {
                doc.reference.delete().await()
            }
        }
    }
}
