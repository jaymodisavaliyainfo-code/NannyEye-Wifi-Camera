package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Device::class, CameraActivity::class, PairedDevice::class, ConnectedViewer::class], version = 2, exportSchema = false)
abstract class DeviceDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun cameraActivityDao(): CameraActivityDao
    abstract fun pairedDeviceDao(): PairedDeviceDao
    abstract fun connectedViewerDao(): ConnectedViewerDao

    companion object {
        @Volatile
        private var INSTANCE: DeviceDatabase? = null

        fun getInstance(context: Context): DeviceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    "device_database"
                ).fallbackToDestructiveMigration()
                .allowMainThreadQueries() // For simplicity in this example
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
