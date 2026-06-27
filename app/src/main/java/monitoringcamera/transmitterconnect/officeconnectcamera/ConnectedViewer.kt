package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connected_viewers")
data class ConnectedViewer(
    @PrimaryKey
    val deviceId: String = "",
    val sessionId: String = "",
    val roomId: String = "",
    val hostId: String = "",
    val peerId: String = "",
    val name: String = "",
    val model: String = "",
    val status: String = "offline",
    val connectionStatus: String = "Disconnected",
    val lastConnectedTime: Long = 0L,
    val lastSeen: Long = 0L,
    val timestamp: Long = 0L
)
