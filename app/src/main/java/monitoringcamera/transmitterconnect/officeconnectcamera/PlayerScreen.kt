package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alexvas.rtsp.widget.RtspDataListener
import com.alexvas.rtsp.widget.RtspStatusListener
import com.alexvas.rtsp.widget.RtspSurfaceView
import monitoringcamera.transmitterconnect.officeconnectcamera.R

@Composable
fun PlayerScreen(
    ip: String,
    port: Int,
    username: String,
    password: String,
    path: String,
    name: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val connectingMsg = stringResource(id = R.string.connecting_to, ip)
    val loadingMsg = stringResource(id = R.string.loading_stream)
    val retryMsg = stringResource(id = R.string.disconnected_retrying)
    val authErrorMsg = stringResource(id = R.string.wrong_credentials)
    val failedMsgTemplate = stringResource(id = R.string.failed_message)

    var statusText by remember { mutableStateOf(connectingMsg) }
    var isError by remember { mutableStateOf(false) }
    var showBackButton by remember { mutableStateOf(false) }

    val recorderManager = remember { RtspRecorderManager(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableIntStateOf(0) }
    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }

    // Recording Timer Logic
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimer = 0
            while (isRecording) {
                delay(1000)
                recordingTimer++
            }
        }
    }

    val uri = remember(ip, port, path) {
        val decodedPath = path.replace("|", "/")
        val builder = Uri.Builder()
            .scheme("rtsp")
            .encodedAuthority("$ip:$port")

        if (decodedPath.contains("?")) {
            val pathOnly = decodedPath.substring(0, decodedPath.indexOf("?"))
            val query = decodedPath.substring(decodedPath.indexOf("?") + 1)
            builder.encodedPath(pathOnly)
            for (param in query.split("&")) {
                val kv = param.split("=")
                if (kv.size == 2) {
                    builder.appendQueryParameter(kv[0], kv[1])
                }
            }
        } else {
            builder.encodedPath(decodedPath)
        }
        builder.build()
    }

    // Keep screen on and immersive mode
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            activity?.requestedOrientation = originalOrientation
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            if (recorderManager.isRecording) {
                recorderManager.stop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                RtspSurfaceView(ctx).apply {
                    init(uri, username, password, null, null)

                    // Use setDataListener to receive raw NAL units for recording
                    setDataListener(object : RtspDataListener {
                        override fun onRtspDataVideoNalUnitReceived(

                            data: ByteArray,
                            offset: Int,
                            length: Int,
                            timestamp: Long
                        ) {
                            val nalUnit = if (offset == 0 && length == data.size) {
                                data
                            } else {
                                data.copyOfRange(offset, offset + length)
                            }
                            recorderManager.onFrame(nalUnit, timestamp * 1000000L)
                        }
                    })

                    setStatusListener(object : RtspStatusListener {
                        override fun onRtspStatusConnecting() {
                            statusText = connectingMsg
                            isError = false
                            showBackButton = false
                        }

                        override fun onRtspStatusConnected() {
                            statusText = loadingMsg
                        }

                        override fun onRtspStatusDisconnecting() {}

                        override fun onRtspStatusDisconnected() {
                            statusText = retryMsg
                            isError = true
                            if (isRecording) {
                                recorderManager.stop()
                                isRecording = false
                            }
                        }

                        override fun onRtspStatusFailedUnauthorized() {
                            statusText = authErrorMsg
                            isError = true
                            showBackButton = true
                            if (isRecording) {
                                recorderManager.stop()
                                isRecording = false
                            }
                        }

                        override fun onRtspStatusFailed(message: String?) {
                            statusText = failedMsgTemplate.format(message ?: "unknown") + "\n\n$uri"
                            isError = true
                            showBackButton = true
                            if (isRecording) {
                                recorderManager.stop()
                                isRecording = false
                            }
                        }

                        override fun onRtspFirstFrameRendered() {
                            statusText = ""
                            isError = false
                            showBackButton = true
                        }

                        override fun onRtspFrameSizeChanged(width: Int, height: Int) {
                            videoWidth = width
                            videoHeight = height
                        }
                    })
                    start(true, false, false)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { it.stop() }
        )

        // Overlay UI
        if (statusText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isError) Color.Black.copy(alpha = 0.7f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isError) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
                    }
                    Text(
                        text = statusText,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.screen_padding)),
                        fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_body).toSp() },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Camera Name and Back Button
        if (showBackButton || isError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.screen_padding_small)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_content_desc),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))

                Text(
                    text = name,
                    color = Color.White,
                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.element_spacing),
                            vertical = dimensionResource(id = R.dimen.spacer_small).div(2)
                        )
                )
            }
        }

        // Record Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = dimensionResource(id = R.dimen.section_spacing)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isRecording) {
                    val infiniteTransition = rememberInfiniteTransition(label = "blink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.element_spacing),
                                vertical = dimensionResource(id = R.dimen.spacer_small).div(2)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.spacer_small))
                                .background(Color.Red.copy(alpha = alpha), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            text = String.format("REC %02d:%02d", recordingTimer / 60, recordingTimer % 60),
                            color = Color.White,
                            fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_small).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                }

                IconButton(
                    onClick = {
                        if (isRecording) {
                            recorderManager.stop()
                            isRecording = false
                            Toast.makeText(context, "Recording saved to Downloads/Sentinel Video", Toast.LENGTH_LONG).show()
                        } else if (videoWidth > 0) {
                            val fileName = recorderManager.start(videoWidth, videoHeight)
                            if (fileName != null) {
                                isRecording = true
                                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to start recording", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Waiting for video stream...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_large))
                        .background(if (isRecording) Color.White else Color.Red, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = if (isRecording) Color.Red else Color.White
                    )
                }
            }
        }
    }
}
