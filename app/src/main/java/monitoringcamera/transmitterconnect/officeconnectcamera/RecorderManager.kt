package monitoringcamera.transmitterconnect.officeconnectcamera

import android.view.Surface
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.io.File
import java.io.IOException

class RecorderManager(private val webRTCManager: WebRTCManager) {

    companion object {
        private const val TAG = "RecorderManager"
        private const val VIDEO_WIDTH = 720
        private const val VIDEO_HEIGHT = 1280
        private const val VIDEO_BITRATE = 3_000_000
        private const val VIDEO_FPS = 30
    }

    private var muxerWrapper: MediaMuxerWrapper? = null
    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null
    private var videoSink: VideoSinkToEncoder? = null
    private var currentOutputPath: String? = null

    @Volatile
    private var isRecording = false

    @Synchronized
    @Throws(IOException::class)
    fun startRecording(outputDir: File, isAudioOnly: Boolean): String? {
        if (isRecording) return null

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw IOException("Failed to create directory")
        }

        val extension = if (isAudioOnly) ".m4a" else ".mp4"
        val prefix = if (isAudioOnly) "REC_" else "MON_"
        val outputPath = File(outputDir, "${prefix}${System.currentTimeMillis()}$extension").absolutePath
        currentOutputPath = outputPath

        try {
            val muxer = MediaMuxerWrapper(outputPath)
            muxerWrapper = muxer
            muxer.setOrientationHint(0)
            muxer.setExpectedTrackCount(if (isAudioOnly) 1 else 2)

            if (!isAudioOnly) {
                val vEncoder = VideoEncoder(muxer, VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_BITRATE, VIDEO_FPS)
                videoEncoder = vEncoder
                vEncoder.start()

                val inputSurface = vEncoder.getInputSurface()
                val eglContext = webRTCManager.getEglBaseContext()

                val vSink = VideoSinkToEncoder(eglContext, inputSurface!!)
                videoSink = vSink

                val videoTrack = webRTCManager.getActiveVideoTrack()
                videoTrack?.addSink(vSink)
            }

            val aEncoder = AudioEncoder(muxer, 44100, 1, 128_000)
            audioEncoder = aEncoder
            aEncoder.start()

            isRecording = true
            return outputPath

        } catch (e: Exception) {
            stopRecording()
            throw IOException(e)
        }
    }

    @Synchronized
    fun stopRecording(): String? {
        if (!isRecording) return null
        isRecording = false

        webRTCManager.getActiveVideoTrack()?.let { track ->
            videoSink?.let { sink -> track.removeSink(sink) }
        }

        videoSink?.release()
        videoEncoder?.stop()
        audioEncoder?.stop()
        muxerWrapper?.stop()

        videoEncoder = null
        audioEncoder = null
        videoSink = null
        muxerWrapper = null

        val path = currentOutputPath
        currentOutputPath = null
        return path
    }

    fun isRecording(): Boolean = isRecording
}
