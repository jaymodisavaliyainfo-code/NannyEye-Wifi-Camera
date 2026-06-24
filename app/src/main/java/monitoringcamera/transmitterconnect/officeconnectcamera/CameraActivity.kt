package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camera_activities")
data class CameraActivity(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val timestamp: Long = 0L,
    val iconType: String = "videocam", // "videocam", "videocam_off", "person", "record"
    val role: String = "camera" // "camera" or "monitor"
)
