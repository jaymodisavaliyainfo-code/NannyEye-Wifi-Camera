package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var ip: String = "",
    var port: Int = 0,
    var username: String = "",
    var password: String = "",
    var path: String = "",
    var brand: String = "",
    var mainStream: Boolean = false,
    var channelCount: Int = 0
)
