package monitoringcamera.transmitterconnect.officeconnectcamera

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.webrtc.SurfaceViewRenderer

sealed class ActiveMonitorItem {
    abstract val sessionId: String
    abstract val name: String

    data class ThisDevice(override val sessionId: String) : ActiveMonitorItem() {
        override val name: String = "This Device"
    }

    data class Remote(override val sessionId: String, override val name: String) : ActiveMonitorItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorWallScreen(
    navController: NavController,
    viewModel: MonitorWallViewModel = viewModel(),
    cameraViewModel: CameraViewModel = viewModel()
) {
    val savedDevices by viewModel.savedDevices.collectAsState()
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    
    val isBroadcasting by cameraViewModel.isBroadcasting.observeAsState(false)
    val mySessionId by cameraViewModel.sessionId.observeAsState("")
    val activePreviewSessions by cameraViewModel.activePreviewSessionIds.observeAsState(emptySet())
    val activePreviewDeviceNames by cameraViewModel.activePreviewDeviceNames.observeAsState(emptyMap())

    val activeItems = remember(isBroadcasting, mySessionId, activePreviewSessions, onlineMonitors, savedDevices, activePreviewDeviceNames) {
        val list = mutableListOf<ActiveMonitorItem>()
        
        if (isBroadcasting && mySessionId.isNotEmpty()) {
            list.add(ActiveMonitorItem.ThisDevice(mySessionId))
        }
        
        activePreviewSessions.forEach { sid ->
            val name = activePreviewDeviceNames[sid] ?: "Remote Camera"
            list.add(ActiveMonitorItem.Remote(sid, name))
        }
        
        savedDevices.filter { 
            it.sessionId !in activePreviewSessions && 
            it.sessionId != mySessionId &&
            onlineMonitors.containsKey(it.deviceId) 
        }.forEach { device ->
            val onlineInfo = onlineMonitors[device.deviceId]
            val sid = onlineInfo?.sessionId ?: device.sessionId
            list.add(ActiveMonitorItem.Remote(sid, device.name))
        }
        
        list.distinctBy { it.sessionId }
    }

    val count = activeItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor Wall", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1116))
            )
        },
        containerColor = Color(0xFF0E1116)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                count == 0 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AddCameraCard(
                            onClick = { navController.navigate("main") },
                            modifier = Modifier.size(280.dp, 300.dp)
                        )
                    }
                }
                count == 1 -> {
                    val item = activeItems[0]
                    MonitorWallTile(
                        item = item,
                        cameraViewModel = cameraViewModel,
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                count == 2 -> {
                    activeItems.forEach { item ->
                        MonitorWallTile(
                            item = item,
                            cameraViewModel = cameraViewModel,
                            navController = navController,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }
                }
                else -> {
                    for (r in 0 until 2) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until 2) {
                                val idx = r * 2 + c
                                if (idx < activeItems.size) {
                                    val item = activeItems[idx]
                                    MonitorWallTile(
                                        item = item,
                                        cameraViewModel = cameraViewModel,
                                        navController = navController,
                                        modifier = Modifier.weight(1f).fillMaxHeight()
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonitorWallTile(
    item: ActiveMonitorItem,
    cameraViewModel: CameraViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val sessionId = item.sessionId
    val isLocal = item is ActiveMonitorItem.ThisDevice
    val isConnected by cameraViewModel.getSessionConnectionState(sessionId).observeAsState(false)
    
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    DisposableEffect(sessionId) {
        onDispose {
            renderer?.let { 
                if (isLocal) cameraViewModel.removeLocalSink(it)
                else cameraViewModel.removeRemoteSink(it)
            }
        }
    }

    LaunchedEffect(renderer, sessionId) {
        renderer?.let { sink ->
            if (isLocal) {
                cameraViewModel.startStreaming(sink, false, sessionId)
            } else {
                cameraViewModel.startRemotePreview(sessionId, sink)
            }
        }
    }

    Card(
        modifier = modifier.clickable {
            if (isLocal) {
                navController.navigate("camera_view/${Uri.encode(sessionId)}")
            } else {
                navController.navigate("viewer/${Uri.encode(sessionId)}")
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        cameraViewModel.initRenderer(this, isLocal = isLocal)
                        renderer = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isConnected) Color.Red else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isConnected) {
                                PulsatingDot()
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isConnected) "LIVE" else "WAITING",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = if (isConnected) Color.Green else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("98%", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                if (isConnected) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Motion",
                            tint = Color.Yellow.copy(alpha = 0.8f),
                            modifier = Modifier.padding(4.dp).size(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLocal) "Front Camera" else "Remote Stream",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (isLocal) {
                            navController.navigate("camera_view/${Uri.encode(sessionId)}")
                        } else {
                            navController.navigate("viewer/${Uri.encode(sessionId)}")
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (!isConnected) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
fun PulsatingDot() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(600),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulsating"
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(Color.White.copy(alpha = alpha), CircleShape)
    )
}
