package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: CameraActivity)

    @Query("SELECT * FROM camera_activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<CameraActivity>>

    @Query("SELECT * FROM camera_activities ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int): Flow<List<CameraActivity>>

    @Query("DELETE FROM camera_activities")
    suspend fun clearAll()

    @Delete
    suspend fun delete(activity: CameraActivity)
}
