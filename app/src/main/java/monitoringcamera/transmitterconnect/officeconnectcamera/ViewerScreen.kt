package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.webrtc.SurfaceViewRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(sessionId: String, onBack: () -> Unit, viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val isConnected by viewModel.getSessionConnectionState(sessionId).observeAsState(false)
    val sessionStatus by viewModel.getSessionStatus(sessionId).observeAsState()
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())

    val deviceName = remember(sessionId, savedDevices) {
        savedDevices.find { it.sessionId == sessionId }?.name
            ?: context.getString(R.string.unknown_camera)
    }

    var showSoundSeekBar by remember { mutableStateOf(false) }
    var soundProgress by remember { mutableFloatStateOf(0.5f) }
    var remoteSink by remember { mutableStateOf<org.webrtc.VideoSink?>(null) }

    val isFrontFacing by viewModel.isFrontFacing.observeAsState(false)

    var showDeclineDialog by remember { mutableStateOf(false) }

    val isRecording by viewModel.isRecording.observeAsState(false)
    val motionDetected by viewModel.motionDetected.observeAsState(false)
    val personDetected by viewModel.personDetected.observeAsState(false)

    // Session Status Observer
    LaunchedEffect(sessionStatus) {
        if (sessionStatus == "closed") {
            viewModel.stopViewing()
            onBack()
        } else if (sessionStatus == "declined") {
            showDeclineDialog = true
            viewModel.stopViewing()
        }
    }

    // Start WebRTC Viewing
    LaunchedEffect(remoteSink, sessionId) {
        val sink = remoteSink
        if (sink != null) {
            viewModel.startViewing(sessionId, sink)
        }
    }

    DisposableEffect(sessionId) {
        onDispose {
            remoteSink?.let { viewModel.removeRemoteSink(it) }
        }
    }

    // Audio Management - Communication Mode & Speakerphone
    DisposableEffect(Unit) {
        val originalMode = audioManager.mode
        val originalSpeaker = audioManager.isSpeakerphoneOn

        onDispose {
            audioManager.isSpeakerphoneOn = originalSpeaker
            audioManager.mode = originalMode
        }
    }

    // Effect to enable speaker when connected
    LaunchedEffect(isConnected) {
        if (isConnected) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
            viewModel.fetchAndSaveDeviceMetadata(sessionId, "receiver")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1116))
    ) {

        // 1. WebRTC Remote Video Renderer (Full Screen)
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        viewModel.initRenderer(this, isLocal = false)
                        remoteSink = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Timer Overlay (matching CameraViewScreen)
            var currentTime by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                while (true) {
                    currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    kotlinx.coroutines.delay(1000)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(color = Color(0xFFFF8A80))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF77AEFF))
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
                    Text(
                        stringResource(id = R.string.connecting_to, deviceName),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
                    )
                    Text(
                        sessionId,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() }
                    )
                }
            }
        }

        // 2. Top Status Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusCapsule(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isConnected) Color.Green else Color.Gray, CircleShape)
                    )
                },
                text = if (isConnected) "LIVE" else "WAITING"
            )

            StatusCapsule(text = deviceName.uppercase())

            // Dummy stats matching image
            StatusCapsule(
                icon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.SignalCellularAlt,
                        null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                },
                text = "98%"
            )

            StatusCapsule(
                icon = {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Videocam,
                        null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                },
                text = "4.2 MB/S"
            )

            if (motionDetected) {
                StatusCapsule(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = if (personDetected) Color.Red else Color.Yellow,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    text = if (personDetected) "PERSON" else "MOTION"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            IconButton(
                onClick = {
                    // Navigate back without stopping the session to allow concurrent viewing
                    onBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 3. Sound Slider Overlay (Vertical)
        if (showSoundSeekBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(44.dp)
                        .height(160.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = soundProgress,
                            onValueChange = {
                                soundProgress = it
                                val streamType = AudioManager.STREAM_VOICE_CALL
                                val maxVol = audioManager.getStreamMaxVolume(streamType)
                                val index = (it * maxVol).toInt()
                                audioManager.setStreamVolume(streamType, index, 0)
                            },
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -90f
                                }
                                .width(120.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF77AEFF),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }

        // 4. Bottom Control Bar (Pill shape matching image)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.9f)
                .height(88.dp),
            color = Color(0xFF161B22).copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snapshot
                IconButton(
                    onClick = {
                        val path = viewModel.takeScreenshot(context, isLocal = false)
                        if (path != null) {
                            android.widget.Toast.makeText(context, "Snapshot saved", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Screenshot,
                        null,
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Volume/Sound Toggle
                IconButton(
                    onClick = { showSoundSeekBar = !showSoundSeekBar }
                ) {
                    Icon(
                        imageVector = if (soundProgress > 0) androidx.compose.material.icons.Icons.AutoMirrored.Filled.VolumeUp else androidx.compose.material.icons.Icons.Default.VolumeOff,
                        contentDescription = "Sound",
                        tint = if (showSoundSeekBar) Color(0xFF77AEFF) else Color.White.copy(alpha = 0.6f)
                    )
                }

                // Record Button (Center)
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            if (isRecording) {
                                viewModel.stopRecording()
                            } else {
                                viewModel.startRecording(context)
                            }
                        },
                    shape = CircleShape,
                    color = if (isRecording) Color.Red.copy(alpha = 0.1f) else Color(0xFF242B33),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = if (isRecording) Color.Red else Color.White
                        ) {
                            Icon(
                                imageVector = if (isRecording) androidx.compose.material.icons.Icons.Default.Stop else androidx.compose.material.icons.Icons.Default.FiberManualRecord,
                                contentDescription = "Record",
                                tint = if (isRecording) Color.White else Color.Red,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Push-to-Talk (Mic)
                var isTalking by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Press -> {
                                            isTalking = true
                                            viewModel.setMicrophoneEnabled(true)
                                        }
                                        PointerEventType.Release -> {
                                            isTalking = false
                                            viewModel.setMicrophoneEnabled(false)
                                        }
                                    }
                                }
                            }
                        },
                    shape = CircleShape,
                    color = if (isTalking) Color(0xFF77AEFF) else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Mic,
                            null,
                            tint = if (isTalking) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Stop/Exit
                IconButton(
                    onClick = {
                        viewModel.stopRemotePreview(sessionId)
                        onBack()
                    }
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Stop,
                        null,
                        tint = Color(0xFFFF8A80)
                    )
                }
            }
        }
    }

    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeclineDialog = false
                viewModel.stopAll()
                onBack()
            },
            title = {
                Text(stringResource(id = R.string.monitor_not_available_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(id = R.string.monitor_not_available_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineDialog = false
                        viewModel.stopViewing()
                        onBack()
                    }
                ) {
                    Text(stringResource(id = R.string.close), color = Color(0xFF77AEFF))
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}
