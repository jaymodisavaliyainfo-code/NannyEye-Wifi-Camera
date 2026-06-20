package monitoringcamera.transmitterconnect.officeconnectcamera

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink
import java.util.Locale

@androidx.camera.core.ExperimentalGetImage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraViewScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    // Timer & Usage State from ViewModel
    val usedSecondsToday by viewModel.usedSecondsToday.observeAsState(0)
    val dailyLimitSeconds by viewModel.dailyLimitSeconds.observeAsState(300)
    var showLimitDialog by remember { mutableStateOf(false) }

    // UI State
    val isRecording by viewModel.isRecording.observeAsState(false)
    var isMuted by remember { mutableStateOf(false) }

    // WebRTC Renderer
    var localSink by remember { mutableStateOf<VideoSink?>(null) }

    val isConnected by viewModel.isConnected.observeAsState(false)
    val sessionStatus by viewModel.sessionStatus.observeAsState()
    val isFrontFacing by viewModel.isFrontFacing.observeAsState(true)
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())

    val myDeviceId by viewModel.myDeviceId.observeAsState("")

    var showDeclineDialog by remember { mutableStateOf(false) }

    // Session Status Observer
    LaunchedEffect(sessionStatus) {
        if (sessionStatus == "closed") {
            viewModel.stopAll()
            onBack()
        } else if (sessionStatus == "declined") {
            showDeclineDialog = true
            // Clear busy status immediately when declined
            viewModel.stop()
        }
    }

    // Start WebRTC Streaming
    LaunchedEffect(localSink) {
        val sink = localSink
        if (sink != null) {
            viewModel.startStreaming(sink, false, sessionId)
        }
    }


    // Timer Logic
    LaunchedEffect(true) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (!showLimitDialog && isConnected) {
                viewModel.incrementUsedSeconds()
                if (usedSecondsToday >= dailyLimitSeconds) {
                    showLimitDialog = true
                }
            }
        }
    }

    // Save usage on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveFinalUsedSeconds()
            viewModel.stopAll()
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Daily Limit Reached") },
            text = { Text("You have reached your daily monitoring limit. Would you like to continue for another minute?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.extendLimit(60)
                    showLimitDialog = false
                }) { Text("Continue (Add 1 Min)") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onBack()
                }) { Text("Disconnect") }
            }
        )
    }

    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeclineDialog = false
                viewModel.stopAll()
                onBack()
            },
            title = {
                Text("Request Declined", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Viewer declined your request.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineDialog = false
                        viewModel.stopAll()
                        onBack()
                    }
                ) {
                    Text("OK", color = Color(0xFF77AEFF))
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // WebRTC Renderer
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    viewModel.initRenderer(this)
                    localSink = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(dimensionResource(id = R.dimen.screen_padding_small)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onBack()
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.PowerSettingsNew,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.element_spacing),
                                vertical = dimensionResource(id = R.dimen.padding_micro)
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.spacer_small))
                                    .background(
                                        if (isConnected) Color.Red else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                            Text(
                                text = if (isConnected) "LIVE" else "WAITING",
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val remaining = maxOf(0, dailyLimitSeconds - usedSecondsToday)
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            remaining / 60,
                            remaining % 60
                        ),
                        color = Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() }
                    )
                }

                Row {

                    IconButton(
                        onClick = { viewModel.switchCamera() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip",
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium_small))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (connectedViewers.isNotEmpty()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.screen_padding_small))
                        .align(Alignment.CenterHorizontally)
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.element_spacing))) {
                        Text(
                            text = "CONNECTED VIEWERS",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                        connectedViewers.forEach { viewer ->
                            Text(
                                text = "• ${viewer.name}",
                                color = Color.Green,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() }
                            )
                        }
                    }
                }
            }

            // Bottom Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.radius_extra_large),
                    topEnd = dimensionResource(id = R.dimen.radius_extra_large)
                )
            ) {
                val isPaired by viewModel.isPaired.observeAsState(false)
                val sessionId by viewModel.sessionId.observeAsState("")
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.screen_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayId = if (sessionId.isNotEmpty()) {
                        if (sessionId.length == 10) {
                            "${sessionId.substring(0, 3)} ${
                                sessionId.substring(
                                    3,
                                    6
                                )
                            } ${sessionId.substring(6)}"
                        } else {
                            sessionId
                        }
                    } else {
                        myDeviceId
                    }
                    Text(
                        text = if (isPaired) "Pairing Code: $displayId" else "Device ID: HIDDEN",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.screen_padding_small))
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding_small)))

                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            usedSecondsToday / 60,
                            usedSecondsToday % 60
                        ),
                        color = if (isRecording) Color.Red else Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
