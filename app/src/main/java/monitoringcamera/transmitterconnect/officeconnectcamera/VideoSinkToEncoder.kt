package monitoringcamera.transmitterconnect.officeconnectcamera

import android.view.Surface
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame
import org.webrtc.VideoSink

class VideoSinkToEncoder(sharedContext: EglBase.Context, surface: Surface) : VideoSink {

    private val eglRenderer: EglRenderer = EglRenderer("RecorderVideoRenderer")
    private var firstTimestampNs: Long = -1

    init {
        // Initialize with the shared context so we can access camera textures
        // EglBase.CONFIG_RECORDABLE is required for MediaCodec compatibility
        eglRenderer.init(sharedContext, EglBase.CONFIG_RECORDABLE, GlRectDrawer())
        
        // Map the encoder's surface to the renderer
        eglRenderer.createEglSurface(surface)
    }

    override fun onFrame(frame: VideoFrame) {
        // Capture the first frame's timestamp to use as a reference point
        if (firstTimestampNs == -1L) {
            firstTimestampNs = frame.timestampNs
        }

        // Create a new VideoFrame with a relative timestamp (starting from 0)
        // This ensures the MP4 timeline starts correctly and doesn't show 
        // a massive (e.g. 29-hour) duration.
        val relativeFrame = VideoFrame(
            frame.buffer,
            frame.rotation,
            frame.timestampNs - firstTimestampNs
        )

        // Render the modified frame onto the MediaCodec Surface
        eglRenderer.onFrame(relativeFrame)
    }

    /**
     * Release EGL resources.
     */
    fun release() {
        eglRenderer.release()
        firstTimestampNs = -1
    }
}
