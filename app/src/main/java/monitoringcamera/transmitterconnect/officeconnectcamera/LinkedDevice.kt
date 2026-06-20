package monitoringcamera.transmitterconnect.officeconnectcamera

import com.google.firebase.Timestamp

data class LinkedDevice(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceModel: String = "",
    val platform: String = "Android",
    val loginTime: Timestamp? = null,
    val lastActive: Timestamp? = null,
    val status: String = "Offline", // "Online", "Offline"
    val isCurrent: Boolean = false
)
