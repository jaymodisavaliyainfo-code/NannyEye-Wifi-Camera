package monitoringcamera.transmitterconnect.officeconnectcamera

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink
import java.text.SimpleDateFormat
import java.util.Date
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

    val currentSessionId by viewModel.sessionId.observeAsState("")
    val isConnected by viewModel.getSessionConnectionState(currentSessionId).observeAsState(false)
    val sessionStatus by viewModel.getSessionStatus(currentSessionId).observeAsState()
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())
    val isBroadcasting by viewModel.isBroadcasting.observeAsState(false)
    val deviceName by viewModel.deviceName.observeAsState("SENTINEL-X1")
    val motionDetected by viewModel.motionDetected.observeAsState(false)
    val personDetected by viewModel.personDetected.observeAsState(false)

    var showDeclineDialog by remember { mutableStateOf(false) }
    var hadViewers by remember { mutableStateOf(false) }

    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Session Status Observer
    LaunchedEffect(sessionStatus) {
        if (sessionStatus == "closed") {
            viewModel.stopStreaming()
            onBack()
        } else if (sessionStatus == "declined") {
            showDeclineDialog = true
            viewModel.stopStreaming()
        }
    }

    // Monitor viewer count
    LaunchedEffect(connectedViewers) {
        if (connectedViewers.isNotEmpty()) {
            hadViewers = true
        } else if (hadViewers && isBroadcasting) {
            viewModel.stopStreaming()
            hadViewers = false
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
            localSink?.let { viewModel.removeLocalSink(it) }
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
            .background(Color(0xFF0E1116))
    ) {
        // 1. WebRTC Renderer (Central/Full)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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

            // 2. Timer Overlay (Center Bottom of video)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp), // Position above control bar
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

        // 3. Top Status Bar Overlay
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
                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(color = Color(0xFFFF8A80))
                    }
                },
                text = "LIVE"
            )

            StatusCapsule(text = deviceName.uppercase())

            StatusCapsule(
                icon = {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                },
                text = "98%"
            )

            StatusCapsule(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
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
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = if (personDetected) Color.Red else Color.Yellow,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    text = if (personDetected) "PERSON" else "MOTION"
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Dedicated Back Button in top right as requested
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 4. Bottom Control Bar (Pill shaped)
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
                ControlIconButton(
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        val path = viewModel.takeScreenshot(context, isLocal = true)
                        if (path != null) {
                            android.widget.Toast.makeText(context, "Snapshot saved", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Mic
                ControlIconButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    onClick = {
                        isMuted = !isMuted
                        viewModel.setMicrophoneEnabled(!isMuted)
                    },
                    isActive = !isMuted
                )

                // Main Record Button (Center)
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
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (isRecording) Color.White else Color(0xFF161B22),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Flip Camera
                ControlIconButton(
                    icon = Icons.Default.FlipCameraAndroid,
                    onClick = { viewModel.switchCamera() }
                )

                // Exit/Stop
                ControlIconButton(
                    icon = Icons.Default.Stop,
                    iconColor = Color(0xFFFF8A80),
                    onClick = {
                        viewModel.stopStreaming()
                        onBack()
                    }
                )
            }
        }

        // Inactive Overlay
        if (!isBroadcasting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Camera is Inactive",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            localSink?.let { sink ->
                                viewModel.startStreaming(sink, isMuted, "new")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF77AEFF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp).width(200.dp)
                    ) {
                        Text("Start Streaming", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCapsule(
    icon: @Composable (() -> Unit)? = null,
    text: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ControlIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = true,
    iconColor: Color = Color.White.copy(alpha = 0.6f)
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) iconColor else Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(24.dp)
        )
    }
}
