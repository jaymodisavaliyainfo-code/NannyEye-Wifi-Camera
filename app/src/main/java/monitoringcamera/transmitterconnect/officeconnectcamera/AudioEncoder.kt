package monitoringcamera.transmitterconnect.officeconnectcamera

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer

class AudioEncoder(
    private val muxerWrapper: MediaMuxerWrapper,
    private val sampleRate: Int,
    private val channelCount: Int,
    private val bitRate: Int
) {
    private companion object {
        private const val TAG = "AudioEncoder"
        private const val MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC
    }

    private var encoder: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var trackIndex = -1

    @Volatile
    private var isRunning = false
    private var recordThread: Thread? = null
    private var drainThread: Thread? = null

    // Normalize audio PTS so MP4 timeline starts from 0
    @Volatile
    private var firstPresentationTimeUs = Long.MIN_VALUE

    @Throws(IOException::class)
    fun start() {
        val format = MediaFormat.createAudioFormat(MIME_TYPE, sampleRate, channelCount)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)

        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE)
            encoder?.let {
                it.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                it.start()
            }

            val channelConfig = if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
            val bufferSize = Math.max(minBufferSize * 2, 4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IOException("AudioRecord initialization failed")
            }

            isRunning = true
            audioRecord?.startRecording()

            recordThread = Thread({ feedAudioToEncoder() }, "AudioRecordFeed")
            recordThread?.start()

            drainThread = Thread({ drainEncoder() }, "AudioEncoderDrain")
            drainThread?.start()
        } catch (e: Exception) {
            release()
            throw IOException(e)
        }
    }

    fun stop() {
        isRunning = false
        recordThread?.let {
            try {
                it.join(2000)
            } catch (ignored: InterruptedException) {
            }
            recordThread = null
        }
        drainThread?.let {
            try {
                it.join(2000)
            } catch (ignored: InterruptedException) {
            }
            drainThread = null
        }
        release()
    }

    private fun release() {
        audioRecord?.let {
            try {
                it.stop()
                it.release()
            } catch (ignored: Exception) {
            }
            audioRecord = null
        }
        encoder?.let {
            try {
                it.stop()
                it.release()
            } catch (ignored: Exception) {
            }
            encoder = null
        }
    }

    private fun feedAudioToEncoder() {
        while (isRunning) {
            val inputBufferIndex = try {
                encoder?.dequeueInputBuffer(10000) ?: -1
            } catch (e: Exception) {
                -1
            }

            if (inputBufferIndex >= 0) {
                val inputBuffer = encoder?.getInputBuffer(inputBufferIndex)
                if (inputBuffer != null) {
                    inputBuffer.clear()
                    val readBytes = audioRecord?.read(inputBuffer, inputBuffer.remaining()) ?: -1
                    if (readBytes > 0) {
                        val presentationTimeUs = System.nanoTime() / 1000
                        encoder?.queueInputBuffer(inputBufferIndex, 0, readBytes, presentationTimeUs, 0)
                    } else if (isRunning) {
                        encoder?.queueInputBuffer(inputBufferIndex, 0, 0, System.nanoTime() / 1000, 0)
                    }
                }
            }
        }

        // Signal EOS
        try {
            val inputBufferIndex = encoder?.dequeueInputBuffer(10000) ?: -1
            if (inputBufferIndex >= 0) {
                encoder?.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    0,
                    System.nanoTime() / 1000,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            }
        } catch (ignored: Exception) {
        }
    }

    private fun drainEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()
        // Keep draining until we receive the real EOS flag from the encoder,
        // even after isRunning becomes false.
        while (true) {
            val outputIndex = try {
                encoder?.dequeueOutputBuffer(bufferInfo, 10000) ?: break
            } catch (e: Exception) {
                break
            }

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    // Only bail if we're stopped AND no more data is expected
                    if (!isRunning) continue
                }
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    encoder?.let { trackIndex = muxerWrapper.addTrack(it.outputFormat) }
                }
                outputIndex >= 0 -> {
                    val encodedData = encoder?.getOutputBuffer(outputIndex)
                    if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        if (bufferInfo.size != 0 && trackIndex >= 0) {
                            // Normalize timestamps
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
                    try { encoder?.releaseOutputBuffer(outputIndex, false) } catch (ignored: Exception) {}

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return
                }
            }
        }
    }
}
