package monitoringcamera.transmitterconnect.officeconnectcamera

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteBuffer

class MediaMuxerWrapper(path: String) {
    private val muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var expectedTrackCount = 0
    private var startedTrackCount = 0
    @Volatile
    private var isStarted = false

    @Synchronized
    fun setExpectedTrackCount(count: Int) {
        expectedTrackCount = count
    }

    @Synchronized
    fun setOrientationHint(degrees: Int) {
        muxer.setOrientationHint(degrees)
    }

    @Synchronized
    fun addTrack(format: MediaFormat): Int {
        if (isStarted) {
            Log.e("MediaMuxerWrapper", "Cannot add track: Muxer already started")
            throw IllegalStateException("Muxer already started")
        }
        val trackIndex = muxer.addTrack(format)
        startedTrackCount++
        Log.d("MediaMuxerWrapper", "Track added: index=$trackIndex, startedTrackCount=$startedTrackCount, expected=$expectedTrackCount")
        if (startedTrackCount == expectedTrackCount) {
            try {
                muxer.start()
                isStarted = true
                Log.d("MediaMuxerWrapper", "Muxer started successfully")
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as java.lang.Object).notifyAll()
            } catch (e: Exception) {
                Log.e("MediaMuxerWrapper", "Failed to start muxer", e)
            }
        }
        return trackIndex
    }

    @Synchronized
    fun awaitMuxerStarted() {
        while (!isStarted) {
            try {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as java.lang.Object).wait(100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }

    fun isStarted(): Boolean = isStarted

    @Synchronized
    fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (isStarted) {
            try {
                if (trackIndex < 0) return
                muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo)
            } catch (e: Exception) {
                Log.e("MediaMuxerWrapper", "Error writing sample data: ${e.message}")
            }
        }
    }

    @Synchronized
    fun stop() {
        Log.d("MediaMuxerWrapper", "Stopping muxer, isStarted=$isStarted")
        if (isStarted) {
            try {
                muxer.stop()
                Log.d("MediaMuxerWrapper", "Muxer stopped successfully")
            } catch (e: Exception) {
                Log.e("MediaMuxerWrapper", "Error stopping muxer", e)
            }
        }
        try {
            muxer.release()
            Log.d("MediaMuxerWrapper", "Muxer released")
        } catch (e: Exception) {
            Log.e("MediaMuxerWrapper", "Error releasing muxer", e)
        }
        isStarted = false
    }
}
