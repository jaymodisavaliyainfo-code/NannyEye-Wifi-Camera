package monitoringcamera.transmitterconnect.officeconnectcamera

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RtspMp4Recorder(
    private val width: Int,
    private val height: Int,
    private val outputPath: String
) {
    private var muxerWrapper: MediaMuxerWrapper? = null
    private var trackIndex = -1
    
    private var firstRealTimeUs: Long = -1
    private var lastPtsUs: Long = -1
    private var lastCameraTimestampNs: Long = -1

    private var sps: ByteBuffer? = null
    private var pps: ByteBuffer? = null

    private var writerThread: HandlerThread? = null
    private var writerHandler: Handler? = null
    private val frameBuffer = mutableListOf<ByteArray>()
    private var isStopped = false
    private var frameCount = 0
    
    @Volatile
    var isActuallyRecording = false
        private set

    companion object {
        private const val TAG = "RtspMp4Recorder"
    }

    @Throws(IOException::class)
    fun start() {
        Log.d(TAG, "Starting recorder session")
        muxerWrapper = MediaMuxerWrapper(outputPath)
        muxerWrapper?.setExpectedTrackCount(1)
        
        writerThread = HandlerThread("RtspRecordWriter")
        writerThread?.start()
        writerHandler = Handler(writerThread!!.looper)
        isStopped = false
        frameCount = 0
        isActuallyRecording = false
        firstRealTimeUs = -1
        lastPtsUs = -1
        lastCameraTimestampNs = -1
    }

    fun setHeaders(spsData: ByteArray?, ppsData: ByteArray?) {
        writerHandler?.post {
            spsData?.let { parseAndStoreHeader(it) }
            ppsData?.let { parseAndStoreHeader(it) }
        }
    }

    private fun parseAndStoreHeader(data: ByteArray) {
        var i = 0
        while (i < data.size - 3) {
            if (isStartCode(data, i)) {
                val offset = if (data[i+2] == 1.toByte()) 3 else 4
                val next = findNextStartCode(data, i + offset)
                val size = if (next == -1) data.size - i else next - i
                val nal = data.copyOfRange(i, i + size)
                val type = (if (nal[2] == 1.toByte()) nal[3] else nal[4]).toInt() and 0x1F
                
                val formatted = formatToAnnexB(nal)
                if (type == 7) {
                    sps = ByteBuffer.allocate(formatted.size).put(formatted)
                    sps?.flip()
                } else if (type == 8) {
                    pps = ByteBuffer.allocate(formatted.size).put(formatted)
                    pps?.flip()
                }
                i = if (next == -1) data.size else next
            } else i++
        }
    }

    fun feedFrame(data: ByteArray, cameraTimestampNs: Long) {
        if (isStopped) return
        writerHandler?.post {
            processPacket(data, cameraTimestampNs)
        }
    }

    private fun processPacket(data: ByteArray, cameraTimestampNs: Long) {
        var i = 0
        while (i < data.size - 3) {
            if (isStartCode(data, i)) {
                val offset = if (data[i+2] == 1.toByte()) 3 else 4
                val next = findNextStartCode(data, i + offset)
                val size = if (next == -1) data.size - i else next - i
                val nal = data.copyOfRange(i, i + size)
                handleNalUnit(nal, cameraTimestampNs)
                i = if (next == -1) data.size else next
            } else i++
        }
    }

    private fun handleNalUnit(nal: ByteArray, cameraTimestampNs: Long) {
        val offset = if (nal[2] == 1.toByte()) 3 else 4
        val type = nal[offset].toInt() and 0x1F
        val formatted = formatToAnnexB(nal)

        if (type == 7) {
            sps = ByteBuffer.allocate(formatted.size).put(formatted); sps?.flip()
            return
        }
        if (type == 8) {
            pps = ByteBuffer.allocate(formatted.size).put(formatted); pps?.flip()
            return
        }

        if (muxerWrapper?.isStarted() == false && sps != null && pps != null) {
            startMuxer()
        }

        if (muxerWrapper?.isStarted() == true) {
            if (cameraTimestampNs != lastCameraTimestampNs && lastCameraTimestampNs != -1L) {
                flushFrame()
            }
            lastCameraTimestampNs = cameraTimestampNs
            frameBuffer.add(formatted)
        }
    }

    private fun startMuxer() {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            format.setByteBuffer("csd-0", sps!!.duplicate())
            format.setByteBuffer("csd-1", pps!!.duplicate())
            trackIndex = muxerWrapper?.addTrack(format) ?: -1
        } catch (e: Exception) { Log.e(TAG, "Muxer error", e) }
    }

    private fun flushFrame() {
        if (frameBuffer.isEmpty()) return
        val wrapper = muxerWrapper ?: return

        var isKeyFrame = false
        val totalSize = frameBuffer.sumOf { it.size }
        val combined = ByteBuffer.allocate(totalSize)
        
        for (nal in frameBuffer) {
            combined.put(nal)
            val off = if (nal[2] == 1.toByte()) 3 else 4
            if ((nal[off].toInt() and 0x1F) == 5) isKeyFrame = true
        }
        combined.flip()

        if (firstRealTimeUs == -1L) {
            if (!isKeyFrame) { frameBuffer.clear(); return }
            firstRealTimeUs = System.currentTimeMillis() * 1000
            isActuallyRecording = true
            Log.i(TAG, "Recording started at real-time 0")
        }

        var ptsUs = (System.currentTimeMillis() * 1000) - firstRealTimeUs
        if (ptsUs <= lastPtsUs) ptsUs = lastPtsUs + 1
        lastPtsUs = ptsUs

        val info = MediaCodec.BufferInfo()
        info.set(0, totalSize, ptsUs, if (isKeyFrame) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
        
        try {
            wrapper.writeSampleData(trackIndex, combined, info)
            frameCount++
        } catch (e: Exception) { Log.e(TAG, "Write error", e) }
        
        frameBuffer.clear()
    }

    private fun isStartCode(data: ByteArray, i: Int): Boolean {
        return data[i] == 0.toByte() && data[i+1] == 0.toByte() && (data[i+2] == 1.toByte() || (data[i+2] == 0.toByte() && data[i+3] == 1.toByte()))
    }

    private fun findNextStartCode(data: ByteArray, start: Int): Int {
        for (i in start until data.size - 3) if (isStartCode(data, i)) return i
        return -1
    }

    private fun formatToAnnexB(nal: ByteArray): ByteArray {
        if (nal[2] == 0.toByte() && nal[3] == 1.toByte()) return nal
        val res = ByteArray(nal.size + 1)
        res[0] = 0; res[1] = 0; res[2] = 0; res[3] = 1
        System.arraycopy(nal, 3, res, 4, nal.size - 3)
        return res
    }

    fun stop() {
        isStopped = true
        isActuallyRecording = false
        val latch = CountDownLatch(1)
        writerHandler?.post {
            try { flushFrame(); muxerWrapper?.stop() } finally { latch.countDown() }
        }
        latch.await(2, TimeUnit.SECONDS)
        writerThread?.quitSafely()
    }
}
