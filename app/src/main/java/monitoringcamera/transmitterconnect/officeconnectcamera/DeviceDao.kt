package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.*

@Dao
interface DeviceDao {

    @Insert
    fun insert(device: Device)

    @Query("SELECT * FROM devices")
    fun getAllDevices(): List<Device>

    @Query("SELECT COUNT(*) FROM devices")
    fun getDeviceCount(): Int

    @Query("DELETE FROM devices WHERE id = :deviceId")
    fun deleteById(deviceId: String)

    @Update
    fun updateDevice(device: Device)

    @Query("SELECT * FROM devices WHERE id = :deviceId LIMIT 1")
    fun getDeviceById(deviceId: String): Device?
}
