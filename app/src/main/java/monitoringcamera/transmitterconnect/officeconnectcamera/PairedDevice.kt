package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDevice(
    @PrimaryKey
    val deviceId: String = "",
    val sessionId: String = "",
    val name: String = "",
    val timestamp: Long = 0L,
    val role: String = "receiver" // "monitor" or "receiver"
)
