package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectedViewerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(viewer: ConnectedViewer)

    @Query("SELECT * FROM connected_viewers ORDER BY timestamp DESC")
    fun getAllConnectedViewers(): Flow<List<ConnectedViewer>>

    @Query("SELECT * FROM connected_viewers WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): ConnectedViewer?

    @Query("DELETE FROM connected_viewers WHERE deviceId = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("DELETE FROM connected_viewers")
    suspend fun clearAll()
}
