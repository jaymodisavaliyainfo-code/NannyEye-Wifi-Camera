package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileInputStream

class RtspRecorderManager(private val context: Context) {
    private var recorder: RtspMp4Recorder? = null
    var isRecording = false
        private set

    private var currentTempFile: File? = null
    
    private var cachedSps: ByteArray? = null
    private var cachedPps: ByteArray? = null

    val isActuallyRecording: Boolean
        get() = recorder?.isActuallyRecording ?: false

    private companion object {
        private const val TAG = "RtspRecorderManager"
    }

    fun start(width: Int, height: Int): String? {
        if (isRecording) return null
        
        val fileName = "RTSP_${System.currentTimeMillis()}.mp4"
        val tempFile = File(context.cacheDir, fileName)
        currentTempFile = tempFile
        
        val newRecorder = RtspMp4Recorder(width, height, tempFile.absolutePath)
        recorder = newRecorder

        return try {
            newRecorder.start()
            // Inject cached headers immediately
            newRecorder.setHeaders(cachedSps, cachedPps)
            isRecording = true
            Log.i(TAG, "Recording started: ${tempFile.absolutePath}")
            fileName
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recorder", e)
            null
        }
    }

    fun stop() {
        Log.i(TAG, "Stopping recording")
        recorder?.stop()
        recorder = null
        isRecording = false
        
        val tempFile = currentTempFile
        if (tempFile != null && tempFile.exists()) {
            val size = tempFile.length()
            if (size > 5000) { // Check for at least 5KB
                saveToPublicDirectory(tempFile)
            } else {
                Log.e(TAG, "Recording too short ($size bytes), discarding.")
                tempFile.delete()
            }
        }
        currentTempFile = null
    }

    private fun saveToPublicDirectory(sourceFile: File) {
        val fileName = sourceFile.name
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Sentinel Video")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { it.copyTo(out) }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Log.i(TAG, "Saved to MediaStore: $uri")
                }
            } else {
                val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Sentinel Video")
                if (!downloadDir.exists()) downloadDir.mkdirs()
                val destFile = File(downloadDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)
                Log.i(TAG, "Saved to public directory: ${destFile.absolutePath}")
            }
            sourceFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving recording", e)
        }
    }

    fun onFrame(data: ByteArray, timestampNs: Long) {
        detectSpsPps(data)
        if (isRecording) {
            recorder?.feedFrame(data, timestampNs)
        }
    }

    private fun detectSpsPps(data: ByteArray) {
        // CCTV often bundles SPS/PPS with other NALs, scan for them
        var i = 0
        while (i < data.size - 4) {
            if (data[i] == 0.toByte() && data[i+1] == 0.toByte() && data[i+2] == 0.toByte() && data[i+3] == 1.toByte()) {
                val type = data[i+4].toInt() and 0x1F
                if (type == 7) cachedSps = extractNal(data, i)
                if (type == 8) cachedPps = extractNal(data, i)
                i += 4
            } else if (data[i] == 0.toByte() && data[i+1] == 0.toByte() && data[i+2] == 1.toByte()) {
                val type = data[i+3].toInt() and 0x1F
                if (type == 7) cachedSps = extractNal(data, i)
                if (type == 8) cachedPps = extractNal(data, i)
                i += 3
            } else i++
        }
    }

    private fun extractNal(data: ByteArray, start: Int): ByteArray {
        var end = start + 4
        while (end < data.size - 3) {
            if (data[end] == 0.toByte() && data[end+1] == 0.toByte() && (data[end+2] == 1.toByte() || (data[end+2] == 0.toByte() && data[end+3] == 1.toByte()))) break
            end++
        }
        return data.copyOfRange(start, end)
    }
}
