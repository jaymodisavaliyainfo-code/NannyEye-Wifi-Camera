package monitoringcamera.transmitterconnect.officeconnectcamera

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer

class VideoEncoder(
    private val muxerWrapper: MediaMuxerWrapper,
    private val width: Int,
    private val height: Int,
    private val bitRate: Int,
    private val frameRate: Int
) {
    private companion object {
        private const val TAG = "VideoEncoder"
        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
        private const val IFRAME_INTERVAL = 10
    }

    private var encoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var trackIndex = -1

    @Volatile
    private var isRunning = false
    private var drainThread: Thread? = null

    // Track the first PTS so the MP4 timeline starts at 0 and not at boot-time
    @Volatile
    private var firstPresentationTimeUs = Long.MIN_VALUE

    fun getInputSurface(): Surface? = inputSurface

    @Throws(IOException::class)
    fun start() {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)

        encoder = MediaCodec.createEncoderByType(MIME_TYPE)
        encoder?.let {
            it.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = it.createInputSurface()
            it.start()
        }

        isRunning = true
        drainThread = Thread({ drainEncoder() }, "VideoEncoderDrain")
        drainThread?.start()
    }

    fun stop() {
        isRunning = false
        encoder?.let {
            try {
                it.signalEndOfInputStream()
            } catch (ignored: Exception) {}
        }
        drainThread?.let {
            try {
                it.join(2000)
            } catch (ignored: InterruptedException) {}
            drainThread = null
        }
        release()
    }

    private fun release() {
        encoder?.let {
            try {
                it.stop()
                it.release()
            } catch (ignored: Exception) {}
            encoder = null
        }
        inputSurface?.let {
            it.release()
            inputSurface = null
        }
    }

    private fun drainEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()
        while (isRunning || encoder != null) {
            val outputIndex: Int = try {
                encoder?.dequeueOutputBuffer(bufferInfo, 10000) ?: break
            } catch (e: Exception) {
                break
            }

            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!isRunning) break
                continue
            }

            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                encoder?.let {
                    trackIndex = muxerWrapper.addTrack(it.outputFormat)
                }
                continue
            }

            if (outputIndex < 0) continue

            val encodedData = encoder?.getOutputBuffer(outputIndex)
            if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                if (bufferInfo.size != 0 && trackIndex >= 0) {
                    // Normalize timestamps: subtract the first PTS so the recording starts at t=0
                    if (firstPresentationTimeUs == Long.MIN_VALUE) {
                        firstPresentationTimeUs = bufferInfo.presentationTimeUs
                    }
                    val adjustedInfo = MediaCodec.BufferInfo().apply {
                        set(
                            bufferInfo.offset,
                            bufferInfo.size,
                            bufferInfo.presentationTimeUs - firstPresentationTimeUs,
                            bufferInfo.flags
                        )
                    }
                    muxerWrapper.awaitMuxerStarted()
                    encodedData.position(adjustedInfo.offset)
                    encodedData.limit(adjustedInfo.offset + adjustedInfo.size)
                    muxerWrapper.writeSampleData(trackIndex, encodedData, adjustedInfo)
                }
            }
            
            try {
                encoder?.releaseOutputBuffer(outputIndex, false)
            } catch (ignored: Exception) {}

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
        }
    }
}
