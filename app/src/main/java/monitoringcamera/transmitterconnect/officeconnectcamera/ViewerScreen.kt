package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.webrtc.SurfaceViewRenderer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(sessionId: String, onBack: () -> Unit, viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val isConnected by viewModel.isConnected.observeAsState(false)
    val sessionStatus by viewModel.sessionStatus.observeAsState()
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

    // Session Status Observer
    LaunchedEffect(sessionStatus) {
        if (sessionStatus == "closed") {
            viewModel.stopAll()
            onBack()
        } else if (sessionStatus == "declined") {
            showDeclineDialog = true
            viewModel.stop()
        }
    }

    // Start WebRTC Viewing
    LaunchedEffect(remoteSink) {
        val sink = remoteSink
        if (sink != null) {
            viewModel.startViewing(sessionId, sink)
        }
    }

    // Audio Management - Communication Mode & Speakerphone
    DisposableEffect(Unit) {
        val originalMode = audioManager.mode
        val originalSpeaker = audioManager.isSpeakerphoneOn

        onDispose {
            audioManager.isSpeakerphoneOn = originalSpeaker
            audioManager.mode = originalMode
            viewModel.stopAll()
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

        // WebRTC Remote Video Renderer - Always present to receive sink
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    viewModel.initRenderer(this, isLocal = true)
                    remoteSink = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

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

        // Overlay Controls
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(dimensionResource(id = R.dimen.spacer_medium)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        viewModel.stopAll()
                        onBack()
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.PowerSettingsNew,
                        contentDescription = stringResource(id = R.string.close),
                        tint = Color.White
                    )
                }

                // Connection Status Badge (matches updateConnectionBadge)
                Surface(
                    color = (if (isConnected) Color(0xFF2E7D32) else Color(0xFF616161)).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large))
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(id = R.dimen.element_spacing),
                            vertical = dimensionResource(id = R.dimen.spacer_small)
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.spacer_small))
                                .background(
                                    if (isConnected) Color(0xFF76FF03) else Color(0xFFBDBDBD),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            text = if (isConnected) stringResource(id = R.string.connected) else stringResource(
                                id = R.string.waiting
                            ),
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard)))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sound Slider Overlay (matches vertical_seek_bar_sound)
            if (showSoundSeekBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(
                            end = dimensionResource(id = R.dimen.screen_padding),
                            bottom = dimensionResource(id = R.dimen.spacer_small)
                        )
                ) {
                    Card(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.option_icon_container))
                            .height(dimensionResource(id = R.dimen.qr_code_size)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large)),
                        border = androidx.compose.foundation.BorderStroke(
                            dimensionResource(id = R.dimen.spacer_tiny),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = dimensionResource(id = R.dimen.screen_padding)),
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
                                    .width(dimensionResource(id = R.dimen.slider_height_large)),
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

            /*// Bottom Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B263B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.section_spacing),
                    topEnd = dimensionResource(id = R.dimen.section_spacing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.screen_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.monitoring_session, deviceName),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.screen_padding_small))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound Toggle (matches iv_sound)
                        IconButton(
                            onClick = {
                                showSoundSeekBar = !showSoundSeekBar
                                if (showSoundSeekBar) {
                                    val streamType = AudioManager.STREAM_VOICE_CALL
                                    val maxVol = audioManager.getStreamMaxVolume(streamType)
                                    val curVol = audioManager.getStreamVolume(streamType)
                                    soundProgress =
                                        if (maxVol > 0) curVol.toFloat() / maxVol else 0f
                                }
                            },
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.status_bar_height))
                                .background(
                                    if (showSoundSeekBar) Color(0xFF77AEFF).copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.05f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (soundProgress > 0) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = stringResource(id = R.string.sound),
                                tint = Color.White,
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.icon_size_medium)) // Was 28dp, 40dp might be big but it's responsive
                                    .graphicsLayer { alpha = if (soundProgress > 0) 1.0f else 0.5f }
                            )
                        }

                        // Push-to-talk button
                        Surface(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.bottom_nav_height)) // Was 84dp, 80dp is close
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
                            color = if (isTalking) Color(0xFF77AEFF) else Color.White,
                            shadowElevation = dimensionResource(id = R.dimen.elevation_large)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(id = R.string.talk),
                                    tint = if (isTalking) Color.White else Color(0xFF0E1116),
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium)) // Was 36dp, 40dp is close
                                )
                            }
                        }

                        // Screenshot/Capture button
                        IconButton(
                            onClick = {
                                val path = viewModel.takeScreenshot(context, isLocal = false)
                                if (path != null) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Screenshot saved: $path",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to capture screenshot",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.status_bar_height))
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Screenshot,
                                contentDescription = stringResource(id = R.string.capture),
                                tint = Color.White,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium)) // Was 28dp, 40dp might be big
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                }
            }*/

            // Bottom Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.radius_extra_large),
                    topEnd = dimensionResource(id = R.dimen.radius_extra_large)
                )
            ) {

                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.screen_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                onClick = {
                                    if (isRecording) {
                                        val path = viewModel.stopRecording()
                                        if (path != null) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Video saved to: $path",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        viewModel.startRecording(context, audioOnly = false)
                                    }
                                },
                                modifier = Modifier.size(dimensionResource(id = R.dimen.bottom_nav_height)),
                                shape = CircleShape,
                                color = if (isRecording) Color.Red else Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                        contentDescription = "Record",
                                        tint = if (isRecording) Color.White else Color.Red,
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
                                    )
                                }
                            }
                        }
                    }
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
                        viewModel.stopAll()
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
