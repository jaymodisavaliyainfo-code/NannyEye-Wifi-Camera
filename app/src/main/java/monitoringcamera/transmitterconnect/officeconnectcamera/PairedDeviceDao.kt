package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: PairedDevice)

    @Query("SELECT * FROM paired_devices ORDER BY timestamp DESC")
    fun getAllPairedDevices(): Flow<List<PairedDevice>>

    @Query("SELECT * FROM paired_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): PairedDevice?

    @Query("DELETE FROM paired_devices WHERE deviceId = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("DELETE FROM paired_devices")
    suspend fun clearAll()
}
