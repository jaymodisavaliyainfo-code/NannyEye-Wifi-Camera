package monitoringcamera.transmitterconnect.officeconnectcamera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Router
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import org.json.JSONObject
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.graphics.drawscope.Fill

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    linkedDevicesViewModel: LinkedDevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBackground = Color(0xFF0E1116)

    LaunchedEffect(Unit) {
        linkedDevicesViewModel.updateActivity()
    }

    // Force portrait orientation when on MainScreen
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {}
    }

    Scaffold(
        containerColor = darkBackground, bottomBar = {
            BottomNavigationBar(pagerState.currentPage) { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        }

    ) { innerPadding ->
        HorizontalPager(
            state = pagerState, modifier = Modifier.padding(innerPadding), userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> DashboardContent(navController, viewModel, authViewModel, linkedDevicesViewModel)
                1 -> RecordsContent(navController)
                2 -> DevicesContent(navController, viewModel)
                3 -> SettingsContent(navController, authViewModel)
            }
        }
    }
}

@Composable
fun DashboardContent(
    navController: NavController,
    viewModel: CameraViewModel,
    authViewModel: AuthViewModel,
    linkedDevicesViewModel: LinkedDevicesViewModel
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = configuration.screenWidthDp >= 600
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    val user by authViewModel.user.collectAsState()
    val fullName = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("full_name", user?.displayName ?: "User") ?: "User"
    }

    val activeSessions = remember { mutableStateListOf<String>() }
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())
    val videoRecords by viewModel.videoRecords.observeAsState(emptyList())
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val myDeviceId by viewModel.myDeviceId.observeAsState("")
    val isConnected by viewModel.isConnected.observeAsState(false)
    val isRemoteConnected by viewModel.isRemoteConnected.observeAsState(false)
    val sessionId by viewModel.sessionId.observeAsState("")
    val qrBitmap by viewModel.qrBitmap.observeAsState()

    var activePreviewSessionId by remember { mutableStateOf<String?>(null) }
    var activePreviewDeviceName by remember { mutableStateOf("") }

    var showQrDialog by remember { mutableStateOf(false) }
    var showAddCameraDialog by remember { mutableStateOf(false) }
    var showManualPairingDialog by remember { mutableStateOf(false) }
    var showIPCameraSetupDialog by remember { mutableStateOf(false) }

    // Close QR dialog automatically when connected
    LaunchedEffect(isConnected) {
        if (isConnected && showQrDialog) {
            showQrDialog = false
        }
    }

    // Start streaming when QR dialog is shown
    LaunchedEffect(showQrDialog) {
        if (showQrDialog) {
            viewModel.startStreaming(null, false)
        }
    }

    // Close QR scanner dialog automatically when remote camera connected
    LaunchedEffect(isRemoteConnected) {
        if (isRemoteConnected && showAddCameraDialog) {
            showAddCameraDialog = false
        }
    }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val sessionsRef = database.getReference("sessions")
        sessionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newSessions = mutableListOf<String>()
                for (sessionSnapshot in snapshot.children) {
                    val sessionId = sessionSnapshot.key
                    if (sessionId != null) {
                        newSessions.add(sessionId)
                    }
                }
                activeSessions.clear()
                activeSessions.addAll(newSessions)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        viewModel.refreshVideoRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C222B)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Good afternoon, $fullName",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@$fullName • SURVEILLANCE HUB".uppercase(),
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 2. Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val onlineCount = savedDevices.count { device ->
                val onlineInfo = onlineMonitors[device.deviceId]
                onlineInfo != null && onlineInfo.sessionId == device.sessionId
            }
            StatCard(
                title = "CAMERAS\nONLINE",
                count = onlineCount.toString(),
                subtitle = if (onlineCount == 0) "No streams active" else "$onlineCount active",
                icon = Icons.Default.Videocam,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ALERTS\nTODAY",
                count = "0",
                subtitle = "Motion events",
                icon = Icons.Outlined.Notifications,
                iconColor = Color(0xFFFF8A65),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Captures Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B232D)
                ) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.folder),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL CAPTURES",
                        color = Color(0xFF9CA3AF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${videoRecords.size} Clips & snaps",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { /* Navigate to Records */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("MANAGE", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Live View Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Live view",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your cameras & this device",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )
            }
            Surface(
                color = Color(0xFF161B22),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { /* Monitor Wall */ }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Adjust,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "MONITOR WALL",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isBroadcasting = sessionId.isNotEmpty()
                ThisDeviceCard(
                    isBroadcasting = isBroadcasting,
                    isConnected = isConnected,
                    sessionId = sessionId,
                    viewModel = viewModel,
                    onStartCamera = {
                        viewModel.generateHostSession()
                        showQrDialog = true
                    }
                )
            }
            activePreviewSessionId?.let { sid ->
                item {
                    RemoteDeviceCard(
                        sessionId = sid,
                        deviceName = activePreviewDeviceName,
                        viewModel = viewModel,
                        onClose = { activePreviewSessionId = null },
                        onClick = { navController.navigate("viewer/${Uri.encode(sid)}") }
                    )
                }
            }
            item {
                AddCameraCard(
                    onClick = { showAddCameraDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Recently Joined
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently joined",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp).clickable { navController.navigate("add_device") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        savedDevices.take(3).forEach { device ->
            val isLive = onlineMonitors[device.deviceId]?.sessionId == device.sessionId
            DeviceRowItem(
                name = device.name,
                status = if (isLive) "ONLINE" else "OFFLINE",
                onActivate = {
                    if (isLive) {
                        navController.navigate("viewer/${Uri.encode(device.sessionId)}")
                    } else {
                        // Handle offline device connection request
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Recent Activity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent activity",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VIEW ALL",
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* All Activity */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ActivityItem(title = "Camera stopped", subtitle = "This device", time = "16:47", icon = Icons.Default.VideocamOff)
        ActivityItem(title = "Monitor connected", subtitle = "samsung SM-S928B", time = "12:48", icon = Icons.Default.Person)
        ActivityItem(title = "Camera started", subtitle = "This device", time = "12:44", icon = Icons.Default.Videocam)

        Spacer(modifier = Modifier.height(32.dp))

        // 7. Upgrade Card
        UpgradeToMaxCard()

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddCameraDialog) {
        AddCameraDialog(
            onDismiss = { showAddCameraDialog = false },
            onManualCode = {
                showAddCameraDialog = false
                showManualPairingDialog = true
            },
            onConnectIPCamera = {
                showAddCameraDialog = false
                showIPCameraSetupDialog = true
            },
            onScanSuccess = { sessionId: String ->
                activePreviewSessionId = sessionId
                activePreviewDeviceName = "Remote Camera"
                viewModel.fetchAndSaveDeviceMetadata(sessionId)
            }
        )
    }

    if (showManualPairingDialog) {
        ManualPairingDialog(
            onDismiss = { showManualPairingDialog = false },
            onConnect = { sessionId: String ->
                showManualPairingDialog = false
                navController.navigate("viewer/${Uri.encode(sessionId)}")
            }
        )
    }

    if (showIPCameraSetupDialog) {
        IPCameraSetupDialog(
            onDismiss = { showIPCameraSetupDialog = false },
            onNavigateToPlayerScreen = { ip, port, user, pass, path, name ->
                showIPCameraSetupDialog = false
                val encodedPath = path.replace("/", "|")
                navController.navigate("player/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${Uri.encode(pass)}/${Uri.encode(encodedPath)}/${Uri.encode(name)}")
            },
            onNavigateToMultiChannel = { ip, port, user, pass, channels, main, brand ->
                showIPCameraSetupDialog = false
                navController.navigate("multi_channel/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${Uri.encode(pass)}/$channels/$main/$brand")
            }
        )
    }

    if (showQrDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Share this camera",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Watch this phone's camera from\nanother phone/browser: in the",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Row {
                        Text(
                            "NannyEye app/web",
                            color = Color(0xFFBBC6E2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            ", add a device and",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp
                        )
                    }
                    Row {
                        Text(
                            "scan this code — or type it in.",
                            color = Color(0xFFBBC6E2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Devices already linked will appear in\nyour list automatically. You don't need\nto reconnect.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.size(240.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = viewModel.sessionId.value ?: "",
                        color = Color(0xFF77AEFF),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.shadow(
                            elevation = 10.dp,
                            spotColor = Color(0xFF77AEFF),
                            ambientColor = Color(0xFF77AEFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(6.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF8A65)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "BROADCASTING. SCAN FROM THE NANNYEYE\nAPP TO WATCH.",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showQrDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.generateHostSession() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D))
                        ) {
                            Text("New code", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = Color(0xFF77AEFF),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = count,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ThisDeviceCard(
    isBroadcasting: Boolean,
    isConnected: Boolean,
    sessionId: String,
    viewModel: CameraViewModel,
    onStartCamera: () -> Unit
) {
    val density = LocalDensity.current
    val cardWidth = 280.dp
    val cardHeight = 300.dp

    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(isBroadcasting) {
        if (isBroadcasting) {
            while (true) {
                currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    Card(
        modifier = Modifier.size(width = cardWidth, height = cardHeight),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        if (isBroadcasting && isConnected) {
            Box(modifier = Modifier.fillMaxSize()) {
                var localSink by remember { mutableStateOf<VideoSink?>(null) }

                LaunchedEffect(localSink) {
                    localSink?.let { sink ->
                        viewModel.startStreaming(sink, false, sessionId)
                    }
                }

                // Live Camera Preview
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            viewModel.initRenderer(this)
                            localSink = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlays
                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Live Indicator (Red if connected, Gray if just sharing)
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(
                                    color = Color.Red,
                                    radius = size.minDimension / 2
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. This Device Label
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "THIS DEVICE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 3. Sharing Session ID Label
                        Surface(
                            color = Color(0xFF77AEFF).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = sessionId,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 4. Clock
                    Text(
                        text = currentTime,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 5. Stop Button (Small close icon in bottom right)
                    IconButton(
                        onClick = { viewModel.stopAll() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        } else if (isBroadcasting && !isConnected) {
            // "Waiting for connection" state
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B232D)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF77AEFF),
                        strokeWidth = 2.dp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Waiting...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Broadcasting session $sessionId. Scan the QR code on the monitor device to connect.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.stopAll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1619))
                ) {
                    Text("Stop Broadcasting", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B232D)
                ) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.round_img),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This device",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Use this phone as a camera — share it to another device.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartCamera,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start camera", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteDeviceCard(
    sessionId: String,
    deviceName: String,
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onClick: () -> Unit
) {
    val cardWidth = 280.dp
    val cardHeight = 300.dp

    Card(
        modifier = Modifier
            .size(width = cardWidth, height = cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            var remoteSink by remember { mutableStateOf<VideoSink?>(null) }

            LaunchedEffect(remoteSink) {
                remoteSink?.let { sink ->
                    viewModel.startRemotePreview(sessionId, sink)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    viewModel.stopRemotePreview()
                }
            }

            // Remote Camera Preview
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        viewModel.initRenderer(this, false)
                        remoteSink = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlays
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Live Indicator (Always live for active preview)
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(6.dp)) {
                            drawCircle(color = Color.Green, radius = size.minDimension / 2)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Device Name Label
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            deviceName.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCameraCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(width = 240.dp, height = 300.dp).clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1116)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFF161B22)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ADD CAMERA",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DeviceRowItem(name: String, status: String, onActivate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1B232D)
            ) {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.smartphone),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "JOINED 3D AGO", // Dummy joined time
                    color = Color(0xFF9CA3AF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = status,
                color = if (status == "ONLINE") Color.Green else Color(0xFF9CA3AF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Activate", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, time: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = Color(0xFF161B22)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF9CA3AF), fontSize = 11.sp)
        }
        Text(time, color = Color(0xFF9CA3AF), fontSize = 11.sp)
    }
}

@Composable
fun UpgradeToMaxCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B232D)
                ) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "UPGRADE TO MAX",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Unlock AI person detection, unlimited device recording, and high-fidelity cloud storage archives.",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Upgrade */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SEE MAX", color = Color.Black, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentSessionItem(
    session: CameraViewModel.SessionRecord,
    isLive: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onConnectLive: () -> Unit,
    navController: NavController,
    viewModel: CameraViewModel,
    onRemove: () -> Unit
) {
    val density = LocalDensity.current
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    var showOptions by remember { mutableStateOf(false) }
    var showBusyDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacer_small))
            .clickable { onExpandToggle() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_standard))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(
                            if (isLive) Color(0xFF77AEFF).copy(alpha = 0.1f) else Color.White.copy(
                                alpha = 0.05f
                            )
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLive) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = if (isLive) Color(0xFF77AEFF) else textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                Column(modifier = Modifier.weight(1f)) {
                    if (session.connectedDevices.isNotEmpty()) {
                        val monitorDetails = session.connectedDevices.joinToString(", ") {
                            val parts = it.split(":")
                            val id = parts.getOrNull(0) ?: ""
                            val name = parts.getOrNull(1) ?: ""
                            if (name.isNotEmpty()) "$name" else id
                        }
                        Text(
                            text = monitorDetails,
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                            maxLines = 1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(
                            id = R.string.session_label, if (session.sessionId.length == 10) {
                                "${session.sessionId.substring(0, 3)} ${
                                    session.sessionId.substring(
                                        3, 6
                                    )
                                } ${session.sessionId.substring(6)}"
                            } else {
                                session.sessionId
                            }
                        ),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() })
                    Text(
                        text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(
                            Date(
                                session.timestamp
                            )
                        ),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLive && !isExpanded) {
                        Text(
                            text = stringResource(id = R.string.live),
                            color = Color.Green,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onConnectLive() })
                    }

                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            null,
                            tint = textGrey,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false },
                        modifier = Modifier.background(cardBackground)
                    ) {
                        DropdownMenuItem(text = {
                            Text(
                                stringResource(id = R.string.remove_device),
                                color = Color.Red,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                        }, onClick = {
                            onRemove()
                            showOptions = false
                        })
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                val primaryGradient =
                    Brush.horizontalGradient(colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B)))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height_small))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(
                            if (isLive) Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)
                                )
                            ) else primaryGradient
                        )
                        .clickable {
                            if (isLive) {
                                navController.navigate("camera_view/${Uri.encode(session.sessionId)}")
                            } else {
                                val connectedDeviceId =
                                    session.connectedDevices.getOrNull(0)?.split(":")?.getOrNull(0)
                                        ?: ""
                                val database = FirebaseDatabase.getInstance()
                                database.getReference("SaveSessions").child(connectedDeviceId).get()
                                    .addOnSuccessListener { snapshot ->
                                        if (snapshot.exists()) {
                                            showBusyDialog = true
                                        } else {
                                            val ref = database.getReference("SaveSessions")
                                                .child(connectedDeviceId.ifEmpty { "unknown" })
                                                .child(session.sessionId)
                                            val saveData = mapOf("status" to "Online")
                                            ref.setValue(saveData)
                                            ref.onDisconnect().removeValue()
                                            viewModel.ViewerDeviceId.value = connectedDeviceId
                                            viewModel.resumeHostSession(session.sessionId)
                                            navController.navigate("camera_view/${Uri.encode(session.sessionId)}")
                                        }
                                    }.addOnFailureListener {
                                        viewModel.resumeHostSession(session.sessionId)
                                        navController.navigate("camera_view/${Uri.encode(session.sessionId)}")
                                    }
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLive) stringResource(id = R.string.continue_broadcasting) else stringResource(
                            id = R.string.start_camera_session
                        ),
                        color = if (isLive) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                }
            }
        }
    }

    if (showBusyDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.device_busy), color = Color.White) },
            text = { Text(stringResource(id = R.string.device_busy_desc), color = Color.White) },
            confirmButton = {
                TextButton(onClick = { showBusyDialog = false }) {
                    Text(stringResource(id = R.string.close), color = Color(0xFF77AEFF))
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun IPCameraStreamItem(
    device: Device,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
    navController: NavController,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    var showOptions by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        IPCameraSetupDialog(deviceToEdit = device, onDismiss = { showEditDialog = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacer_small))
            .clickable { onExpandToggle() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_standard))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(Color(0xFFBBC6E2).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFFBBC6E2),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        color = Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.brand + stringResource(id = R.string.interpunct_separator) + device.ip,
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() })
                }

                if (!isExpanded) {
                    Text(
                        text = stringResource(id = R.string.online),
                        color = Color.Green,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { showOptions = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        tint = textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }

                DropdownMenu(
                    expanded = showOptions,
                    onDismissRequest = { showOptions = false },
                    modifier = Modifier.background(cardBackground)
                ) {
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.edit_camera),
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                    }, onClick = {
                        showEditDialog = true
                        showOptions = false
                    })
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.remove_camera),
                            color = Color.Red,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                    }, onClick = {
                        onRemove()
                        showOptions = false
                    })
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                val primaryGradient =
                    Brush.horizontalGradient(colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B)))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height_small))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(primaryGradient)
                        .clickable {
                            val channelCount = device.channelCount
                            if (channelCount == 0) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Please enter channelCount",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else if (channelCount == 1) {
                                val encodedPath = device.path.replace("/", "|")
                                val encIp = Uri.encode(device.ip)
                                val encUser = Uri.encode(device.username)
                                val encPass = Uri.encode(device.password)
                                val encPathArg = Uri.encode(encodedPath)
                                val encName = Uri.encode(device.name)
                                navController.navigate("player/$encIp/${device.port}/$encUser/$encPass/$encPathArg/$encName")
                            } else if (channelCount > 1) {
                                val encIp = Uri.encode(device.ip)
                                val encUser = Uri.encode(device.username)
                                val encPass = Uri.encode(device.password)
                                navController.navigate("multi_channel/$encIp/${device.port}/$encUser/$encPass/${device.channelCount}/${device.mainStream}/${device.brand}")
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.connect),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                }
            }
        }
    }
}

@Composable
fun StreamItem(
    sessionId: String,
    deviceId: String,
    name: String,
    isActive: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    navController: NavController,
    isMySession: Boolean = false,
    isOccupied: Boolean = false,
    role: String = "receiver",
    cameraViewModel: CameraViewModel? = null,
    onRemove: () -> Unit = {}
) {

    val density = LocalDensity.current
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    var showOptions by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }
    var showBusyDialog by remember { mutableStateOf(false) }
    val viewerId = cameraViewModel?.myDeviceId?.value ?: ""
    var wasActive by remember { mutableStateOf(isActive) }
    val viewerCount = remember { mutableIntStateOf(0) }
    var isSessionOnline by remember { mutableStateOf(false) }
    var currentViewerDeviceId: String? = null
    var peerConnection: String? = null

    // Real-time listener for the specific session ID in SaveSessions
    DisposableEffect(sessionId, deviceId) {
        val database = FirebaseDatabase.getInstance()
        val sessionRef = database.getReference("SaveSessions").child(deviceId).child(sessionId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isSessionOnline = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                isSessionOnline = false
            }
        }

        sessionRef.addValueEventListener(listener)
        onDispose {
            sessionRef.removeEventListener(listener)
        }
    }

    // Auto-show dialog when isActive transitions (monitor goes online → viewer sees it)
    LaunchedEffect(isActive) {
        if (isActive && !wasActive) {
            // Only show the popup when it first becomes active (transitions from false to true)
            if (!(role == "monitor" || isMySession)) {
                val database = FirebaseDatabase.getInstance()
                val deviceRequestRef = database.getReference("SaveSessions").child(deviceId)

                deviceRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isSessionValid = snapshot.hasChild(sessionId)

                        if (isSessionValid) {
                            // Session exists in SaveSessions, meaning a viewer is already connected
                            showBusyDialog = true
                            Log.e("check8951", "showConnectDialog3")
                        } else {
                            // Free to connect
                            showConnectDialog = true
                            Log.e("check8951", "showConnectDialog4")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showConnectDialog = false
                    }
                })
            }
        }
        wasActive = isActive
    }

    DisposableEffect(sessionId, isActive) {
        var listener: ValueEventListener? = null
        if (isActive) {
            val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/viewers")
            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewerCount.intValue = snapshot.childrenCount.toInt()

                }

                override fun onCancelled(error: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
        } else {
            viewerCount.intValue = 0
        }

        onDispose {
            listener?.let {
                FirebaseDatabase.getInstance().getReference("sessions/$sessionId/viewers")
                    .removeEventListener(it)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacer_small))
            .clickable {
                if (isActive) {
                    if ((isOccupied || isSessionOnline) && !(role == "monitor" || isMySession)) {
                        showBusyDialog = true
                    } else if (role == "monitor" || isMySession) {
                        navController.navigate("camera_view/${Uri.encode(sessionId)}")
                    } else {
                        showConnectDialog = true
                        Log.e("check8951", "showConnectDialog5")
                    }
                } else {
                    onExpandToggle()
                }
            },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_size_standard))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(
                            if (isActive) Color(0xFF77AEFF).copy(alpha = 0.1f) else Color.White.copy(
                                alpha = 0.05f
                            )
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = if (isActive) Color(0xFF77AEFF) else textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                }
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive && viewerCount.intValue > 0) {
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                            Surface(
                                color = Color(0xFF77AEFF).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = dimensionResource(id = R.dimen.spacer_micro),
                                        vertical = dimensionResource(id = R.dimen.spacer_nano)
                                    ), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        null,
                                        tint = Color(0xFF77AEFF),
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_tiny))
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_nano)))
                                    Text(
                                        text = viewerCount.intValue.toString(),
                                        color = Color(0xFF77AEFF),
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = when {
                            isActive -> stringResource(id = R.string.online)
                            else -> stringResource(id = R.string.offline)
                        },
                        color = when {
                            isActive -> Color(0xFF4CAF50)
                            else -> textGrey
                        },
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive && !isExpanded) {
                        Text(
                            text = stringResource(id = R.string.live),
                            color = Color.Green,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            null,
                            tint = textGrey,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false },
                        modifier = Modifier.background(cardBackground)
                    ) {
                        DropdownMenuItem(text = {
                            Text(
                                stringResource(id = R.string.remove_device),
                                color = Color.Red,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                        }, onClick = {
                            onRemove()
                            showOptions = false
                        })
                    }
                }
            }


            if (isExpanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                val primaryGradient =
                    Brush.horizontalGradient(colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B)))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height_small))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                        .background(
                            if (isActive) Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)
                                )
                            ) else primaryGradient
                        )
                        .clickable {
                            if (isActive) {
                                if ((isOccupied || isSessionOnline) && !(role == "monitor" || isMySession)) {
                                    showBusyDialog = true
                                } else if (role == "monitor" || isMySession) {
                                    navController.navigate("camera_view/${Uri.encode(sessionId)}")
                                } else {
                                    showConnectDialog = true
                                    Log.e("check8951", "showConnectDialog6")
                                }
                            } else {
                                if (isMySession) {
                                    val database = FirebaseDatabase.getInstance()
                                    // Check if this device ID exists in SaveSessions
                                    database.getReference("SaveSessions").child(deviceId).get()
                                        .addOnSuccessListener { saveSnapshot ->
                                            if (saveSnapshot.exists()) {
                                                showBusyDialog = true
                                            } else {
                                                // Also check the WebRTC answer node as a secondary guard
                                                cameraViewModel?.resumeHostSession(sessionId)
                                                navController.navigate(
                                                    "camera_view/${
                                                        Uri.encode(
                                                            sessionId
                                                        )
                                                    }"
                                                )
                                            }
                                        }.addOnFailureListener {
                                            cameraViewModel?.resumeHostSession(sessionId)
                                            navController.navigate(
                                                "camera_view/${
                                                    Uri.encode(
                                                        sessionId
                                                    )
                                                }"
                                            )
                                        }
                                } else {
                                    if (sessionId.isNotEmpty()) {

                                        val database = FirebaseDatabase.getInstance()
                                        database.getReference("SaveSessions").child(deviceId).get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.exists()) {
                                                    // Device ID matches a key in SaveSessions, so it's already busy
                                                    showBusyDialog = true
                                                } else {
                                                    // Not busy, proceed to start session
                                                    val ref = database.getReference("SaveSessions")
                                                        .child(deviceId.ifEmpty { "unknown" })
                                                        .child(sessionId)

                                                    val saveData = mapOf("status" to "Online")
                                                    ref.setValue(saveData)
                                                    ref.onDisconnect().removeValue()
                                                    cameraViewModel?.ViewerDeviceId?.value =
                                                        deviceId

                                                    navController.navigate(
                                                        "viewer/${
                                                            Uri.encode(
                                                                sessionId
                                                            )
                                                        }"
                                                    )
                                                }
                                            }.addOnFailureListener {
                                                // Fallback for network issues
                                                navController.navigate(
                                                    "viewer/${
                                                        Uri.encode(
                                                            sessionId
                                                        )
                                                    }"
                                                )
                                            }
                                    }
                                }
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (role == "monitor" || isMySession) {
                            if (isActive) stringResource(id = R.string.continue_broadcasting) else stringResource(
                                id = R.string.start_camera_session
                            )
                        } else {
                            stringResource(id = R.string.connect)
                        },
                        color = if (isActive) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                }
            }
        }
    }

    if (showBusyDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = stringResource(id = R.string.device_busy)) },
            text = { Text(text = stringResource(id = R.string.device_busy_desc)) },
            confirmButton = {
                TextButton(onClick = { showBusyDialog = false }) {
                    Text(text = stringResource(id = android.R.string.ok), color = Color(0xFF77AEFF))
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = {
                Text(
                    text = "Device Online", fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Do you want to connect to this device?")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (role == "monitor" || isMySession) {
                        showConnectDialog = false
                        if (!isActive) {
                            cameraViewModel?.resumeHostSession(sessionId)
                        }
                        navController.navigate("camera_view/${Uri.encode(sessionId)}")
                    } else {
                        if (isActive) {
                            val database = FirebaseDatabase.getInstance()
                            val ref = database.getReference("SaveSessions")
                                .child(deviceId.ifEmpty { "unknown" }).child(sessionId)

                            val saveData = mapOf("status" to "Online")
                            ref.setValue(saveData)
                            ref.onDisconnect().removeValue()
                            cameraViewModel?.ViewerDeviceId?.value = deviceId
                            navController.navigate("viewer/${Uri.encode(sessionId)}")
                        } else {
                            showConnectDialog = false
                        }
                    }
                }) {
                    Text("Accept", color = Color(0xFF77AEFF))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConnectDialog = false
                    cameraViewModel?.declineRemoteSession(sessionId, deviceId)
                }) {
                    Text("Cancel", color = Color.Red)
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsContent(
    navController: NavController,
    viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showRecordDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    var selectedDateText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    val context = LocalContext.current
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        selectedDateText = sdf.format(Date())
    }

    val videoRecords by viewModel.videoRecords.observeAsState(emptyList())
    val isLoadingVideos by viewModel.isLoadingVideos.observeAsState(false)
    val recordVideos by viewModel.recordVideos.observeAsState(emptyList())
    val monitorVideos by viewModel.monitorVideos.observeAsState(emptyList())
    val ipCameraVideos by viewModel.ipCameraVideos.observeAsState(emptyList())

    val filteredVideoRecords = remember(
        videoRecords, selectedFilters, recordVideos, monitorVideos, ipCameraVideos, searchQuery
    ) {
        val baseList = if (selectedFilters.isEmpty() || selectedFilters.contains("All")) {
            videoRecords
        } else {
            val result = mutableListOf<CameraViewModel.VideoRecord>()
            if (selectedFilters.contains("Record")) result.addAll(recordVideos)
            if (selectedFilters.contains("Monitor")) result.addAll(monitorVideos)
            if (selectedFilters.contains("IP Camera")) result.addAll(ipCameraVideos)
            result.distinctBy { it.file.absolutePath }
        }

        if (searchQuery.isEmpty()) {
            baseList.sortedByDescending { it.file.lastModified() }
        } else {
            baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                .sortedByDescending { it.file.lastModified() }
        }
    }

    val storagePermission =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshVideoRecords()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, storagePermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.refreshVideoRecords()
        } else {
            storagePermissionLauncher.launch(storagePermission)
        }
    }

    val activity = context as Activity
    val textGrey = Color(0xFF9CA3AF)
    val cardBackground = Color(0xFF181C22)
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
    )

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = configuration.screenWidthDp >= 600

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (cameraGranted && audioGranted) {
            navController.navigate("record_video")
        } else {
            // Check if user denied permanently
            val cameraPermanentlyDenied =
                !cameraGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.CAMERA
                )
            val audioPermanentlyDenied =
                !audioGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.RECORD_AUDIO
                )

            if (cameraPermanentlyDenied || audioPermanentlyDenied) {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        CustomPermissionDialog(onDismiss = { showPermissionDialog = false }, onOpenSettings = {
            showPermissionDialog = false
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        })
    }

    if (showRecordDialog) {
        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            containerColor = Color(0xFF1B1F26),
            title = null,
            text = {
                Column {
                    Button(
                        onClick = {
                            showRecordDialog = false
                            val cameraPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            )
                            val audioPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            )

                            if (cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED) {
                                navController.navigate("record_video")
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.button_height_small)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3542))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Smartphone,
                                null,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                            Text(
                                stringResource(id = R.string.record_on_phone),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                    Button(
                        onClick = { showRecordDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.button_height_small)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3542))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Podcasts,
                                null,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                            Text(
                                stringResource(id = R.string.online_broadcast),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                        }
                    }
                }
            },
            confirmButton = { })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.screen_padding))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.spacer_medium)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.records),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(id = R.string.history_archives),
                                color = textGrey,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                fontWeight = FontWeight.Bold,
                                letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
                        }
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                selectedFilters = emptySet()
                                viewModel.refreshVideoRecords(null)
                                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                selectedDateText = sdf.format(Date())
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset Filter",
                                tint = Color.White,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium_small))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search records...",
                                color = textGrey,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = textGrey,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = textGrey,
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                    )
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardBackground,
                            unfocusedContainerColor = cardBackground,
                            focusedBorderColor = Color(0xFF77AEFF),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true)

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                    // Date Dropdown
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker = true
                            },
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                        colors = CardDefaults.cardColors(containerColor = cardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                tint = textGrey,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.element_spacing)))
                            Text(
                                text = selectedDateText,
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = textGrey,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                            )
                        }
                    }

                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()
                        val customDatePickerColors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF242B30),
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = Color.White,
                            subheadContentColor = Color.White,
                            navigationContentColor = Color.White,
                            yearContentColor = Color.White,
                            currentYearContentColor = Color.White,
                            selectedYearContentColor = Color(0xFF00344B),
                            selectedYearContainerColor = Color(0xFF81CFFF),
                            dayContentColor = Color.White,
                            disabledDayContentColor = Color.White,
                            selectedDayContentColor = Color(0xFF00344B),
                            selectedDayContainerColor = Color(0xFF81CFFF),
                            todayContentColor = Color(0xFF81CFFF),
                            todayDateBorderColor = Color(0xFF81CFFF),
                            dayInSelectionRangeContentColor = Color.White,
                            dayInSelectionRangeContainerColor = Color(0xFF81CFFF).copy(alpha = 0.2f)
                        )

                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            colors = DatePickerDefaults.colors(
                                containerColor = Color(0xFF242B30)
                            ),
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Date(millis)
                                        val sdf =
                                            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                        selectedDateText = sdf.format(date)
                                        viewModel.refreshVideoRecords(date)
                                    }
                                    showDatePicker = false
                                }) {
                                    Text(
                                        "OK",
                                        color = Color(0xFF81CFFF),
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text(
                                        "Cancel",
                                        color = Color(0xFF81CFFF),
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                                }
                            }) {
                            DatePicker(
                                state = datePickerState, colors = customDatePickerColors
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                    // Filter Dropdown and Chips
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Toggle filter row: if filters are active, clear them to hide the row.
                                    // If no filters are active, show the row with "All" selected.
                                    if (selectedFilters.isNotEmpty()) {
                                        selectedFilters = emptySet()
                                    } else {
                                        selectedFilters = setOf("All")
                                    }
                                },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                            colors = CardDefaults.cardColors(containerColor = cardBackground)
                        ) {
                            Row(
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    null,
                                    tint = textGrey,
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.element_spacing)))
                                Text(
                                    stringResource(id = R.string.all_devices),
                                    color = Color.White,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (selectedFilters.isNotEmpty()) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    null,
                                    tint = textGrey,
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                )
                            }
                        }

                        if (selectedFilters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                            val availableFilters = listOf("All", "Record", "Monitor", "IP Camera")
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacer_small))
                            ) {
                                items(availableFilters) { filter ->
                                    val isSelected = selectedFilters.contains(filter)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedFilters = if (filter == "All") {
                                                // Clicking the "All" chip clears all filters and hides the filter tab
                                                emptySet()
                                            } else {
                                                if (isSelected) {
                                                    selectedFilters - filter
                                                } else {
                                                    // Selecting a specific filter removes "All"
                                                    (selectedFilters - "All") + filter
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = filter,
                                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            labelColor = Color.White.copy(alpha = 0.6f),
                                            selectedContainerColor = Color(0xFFBBC6E2),
                                            selectedLabelColor = Color(0xFF101B30),
                                            selectedLeadingIconColor = Color(0xFF101B30),
                                            selectedTrailingIconColor = Color(0xFF101B30)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color.Transparent,
                                            selectedBorderColor = Color.Transparent,
                                            enabled = true,
                                            selected = isSelected
                                        ),
                                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large)),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (filter) {
                                                    "Record" -> Icons.Default.Smartphone
                                                    "Monitor" -> Icons.Default.Podcasts
                                                    "IP Camera" -> Icons.Default.Videocam
                                                    else -> Icons.Default.History
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(
                                                    dimensionResource(id = R.dimen.icon_size_small).div(
                                                        1.3f
                                                    )
                                                )
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove filter",
                                                    modifier = Modifier
                                                        .size(dimensionResource(id = R.dimen.spacer_medium))
                                                        .clickable {
                                                            selectedFilters =
                                                                selectedFilters - filter
                                                        })
                                            }
                                        } else null)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                }

                if (isLoadingVideos) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.section_spacing)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF77AEFF))
                        }
                    }
                } else if (filteredVideoRecords.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                                .background(cardBackground)
                                .padding(dimensionResource(id = R.dimen.section_spacing)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.norecord_img),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(
                                        if (isTablet) screenWidthDp * 0.15f
                                        else screenWidthDp * 0.25f
                                    )
                                    .widthIn(max = 200.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                            Text(
                                stringResource(id = R.string.no_recordings_yet),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() },
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                            Text(
                                stringResource(id = R.string.no_recordings_description),
                                color = textGrey,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                textAlign = TextAlign.Center,
                                lineHeight = with(density) { dimensionResource(id = R.dimen.text_title).toSp() })
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                    }
                } else {
                    items(filteredVideoRecords) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(id = R.dimen.spacer_small)),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                            colors = CardDefaults.cardColors(containerColor = cardBackground)
                        ) {
                            Row(
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.element_spacing)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.thumbnail_size))
                                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                                        .background(Color.Black)
                                        .clickable {
                                            try {
                                                val uri =
                                                    androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.provider",
                                                        record.file
                                                    )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "video/*")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Could not open video",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    if (record.thumbnail != null) {
                                        Image(
                                            bitmap = record.thumbnail.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Semi-transparent overlay to make play icon visible
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.3f))
                                        )
                                    }
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.section_spacing))
                                    )
                                }
                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.name,
                                        color = Color.White,
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_micro)))
                                    Text(
                                        text = "${record.formattedDate} • ${record.duration}",
                                        color = textGrey,
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() })
                                    Text(
                                        text = record.size,
                                        color = textGrey.copy(alpha = 0.7f),
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() })
                                }
                                IconButton(onClick = {
                                    fileToDelete = record.file
                                    showDeleteSheet = true
                                }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                                    )
                                }
                            }
                        }
                    }
                }

                /* item {
                     // Secure Storage Card
                     Card(
                         modifier = Modifier.fillMaxWidth(),
                         shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                         colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                     ) {
                         Column(
                             modifier = Modifier
                                 .background(primaryGradient)
                                 .fillMaxWidth()
                         ) {
                             Column(
                                 modifier = Modifier.padding(dimensionResource(id = R.dimen.screen_padding))
                             ) {
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Column {
                                         Text(
                                             stringResource(id = R.string.cloud),
                                             color = textGrey,
                                             fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                                             fontWeight = FontWeight.Bold
                                         )
                                         Text(
                                             stringResource(id = R.string.secure),
                                             color = textGrey,
                                             fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                                             fontWeight = FontWeight.Bold
                                         )
                                     }
                                 }
                                 Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                                 Text(
                                     text = stringResource(id = R.string.secure_video_storage),
                                     color = Color.White,
                                     fontSize = with(density) {
                                         dimensionResource(id = R.dimen.text_title).toSp()
                                             .times(1.4f)
                                     },
                                     fontWeight = FontWeight.Bold,
                                     lineHeight = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() })
                                 Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                                 Text(
                                     text = stringResource(id = R.string.secure_storage_description),
                                     color = textGrey,
                                     fontSize = with(density) {
                                         dimensionResource(id = R.dimen.text_small).toSp()
                                             .times(0.9f)
                                     },
                                     lineHeight = with(density) {
                                         dimensionResource(id = R.dimen.text_body).toSp().times(1.1f)
                                     })
                                 Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                                 Button(
                                     onClick = { },
                                     modifier = Modifier.height(dimensionResource(id = R.dimen.button_height_small)),
                                     colors = ButtonDefaults.buttonColors(
                                         containerColor = Color(
                                             0xFF242A35
                                         )
                                     ),
                                     shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard))
                                 ) {
                                     Text(
                                         stringResource(id = R.string.activate_sentinel_plus),
                                         color = textGrey,
                                         fontWeight = FontWeight.Bold,
                                         fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                                 }
                             }
                             // Image Placeholder
                             Box(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .height(dimensionResource(id = R.dimen.spacer_small).times(1.2f))
                                     .background(
                                         Brush.verticalGradient(
                                             listOf(
                                                 Color.Transparent,
                                                 Color(0xFF001A26).copy(alpha = 0.5f)
                                             )
                                         )
                                     ), contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     Icons.Default.Lock,
                                     contentDescription = null,
                                     tint = Color(0xFF77AEFF).copy(alpha = 0.3f),
                                     modifier = Modifier.size(
                                         dimensionResource(id = R.dimen.icon_size_large).times(
                                             1.5f
                                         )
                                     )
                                 )
                             }
                         }
                     }
                     Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                 }*/

                item {
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(id = R.dimen.bottom_nav_height).times(
                                1.2f
                            )
                        )
                    )
                }
            }
        }


        // Record video button - fixed at bottom

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0E1116).copy(alpha = 0.9f),
                            Color(0xFF0E1116)
                        ), startY = 0f, endY = 100f
                    )
                )
                .padding(bottom = dimensionResource(id = R.dimen.spacer_medium)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isTablet) 0.35f else 0.55f)
                        .widthIn(max = 300.dp)
                        .height(dimensionResource(id = R.dimen.button_height))
                        .shadow(
                            elevation = dimensionResource(id = R.dimen.elevation_large),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                            ambientColor = Color(0xFF77AEFF).copy(alpha = 0.5f),
                            spotColor = Color(0xFF77AEFF).copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                        .background(primaryGradient)
                        .clickable {
                            val cameraPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            )
                            val audioPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            )

                            if (cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED) {
                                navController.navigate("record_video")
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.round_img),
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            stringResource(id = R.string.record_video),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() })
                    }
                }
            }
        }
    }

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            containerColor = Color(0xFF1B1F26),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding))
                    .padding(bottom = dimensionResource(id = R.dimen.section_spacing))
            ) {
                Text(
                    text = stringResource(id = R.string.recording_options),
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.element_spacing))
                )

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        fileToDelete?.delete()
                        viewModel.refreshVideoRecords()
                        showDeleteSheet = false
                    }
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard))
                    )
                    .padding(dimensionResource(id = R.dimen.card_padding)),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                    Text(
                        stringResource(id = R.string.delete_recording),
                        color = Color.Red,
                        fontWeight = FontWeight.Medium,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                }
            }
        }
    }
}

@Composable
fun DevicesContent(navController: NavController, viewModel: CameraViewModel) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = configuration.screenWidthDp >= 600
    val textGrey = Color(0xFF9CA3AF)
    val cardBackground = Color(0xFF1B1F26)
    val upgradeGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4A90E2), Color(0xFF50E3C2))
    )
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
    )

    val deviceName by viewModel.deviceName.observeAsState("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
    val roomDevices by viewModel.roomDevices.observeAsState(emptyList())
    var expandedIPDeviceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Data is now loaded via Firestore listeners in CameraViewModel
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.screen_padding))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.spacer_medium)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.devices),
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.manage_ecosystem),
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() })

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))

                /*// Upgrade Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .background(upgradeGradient)
                            .padding(dimensionResource(id = R.dimen.card_padding))
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(id = R.string.upgrade_pro),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(id = R.string.upgrade_description),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() })
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))*/

                // Monitors Section
                Text(
                    stringResource(id = R.string.monitors),
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("monitor_details") },
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                    colors = CardDefaults.cardColors(containerColor = cardBackground)
                ) {
                    Row(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.option_icon_container))
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_small)))
                                .background(Color(0xFF31353C)), contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.smartphone),
                                contentDescription = null,
                                tint = Color(0xFF77AEFF),
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium_small))
                            )
                        }
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deviceName,
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stringResource(id = R.string.active_now),
                                color = Color(0xFF4CAF50),
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() })
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                // Cameras Section
                Text(
                    stringResource(id = R.string.cameras),
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                if (roomDevices.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                            .background(cardBackground)
                            .padding(dimensionResource(id = R.dimen.section_spacing)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.videocam_of),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(
                                    if (isTablet) screenWidthDp * 0.15f
                                    else screenWidthDp * 0.25f
                                )
                                .widthIn(max = 200.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                        Text(
                            stringResource(id = R.string.no_cameras_connected),
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            stringResource(id = R.string.add_camera_monitor_desc),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                            textAlign = TextAlign.Center,
                            lineHeight = with(density) { dimensionResource(id = R.dimen.text_title).toSp() })
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
                }

                if (roomDevices.isNotEmpty()) {
                    roomDevices.forEach { device ->
                        val isExpanded = expandedIPDeviceId == device.id
                        IPCameraStreamItem(
                            device = device,
                            isExpanded = isExpanded,
                            onExpandToggle = {
                                expandedIPDeviceId = if (isExpanded) null else device.id
                            },
                            navController = navController,
                            onRemove = { viewModel.deleteRoomDevice(device) })
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(id = R.dimen.bottom_nav_height).times(
                            1.2f
                        )
                    )
                )
            }

        }

        /*Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0E1116).copy(alpha = 0.9f),
                            Color(0xFF0E1116)
                        ), startY = 0f, endY = 100f
                    )
                )
                .padding(bottom = dimensionResource(id = R.dimen.spacer_medium)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isTablet) 0.35f else 0.55f)
                    .widthIn(max = 320.dp)
                    .height(dimensionResource(id = R.dimen.button_height))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                    .background(primaryGradient)
                    .clickable { navController.navigate("add_device") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AddCircleOutline,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                    Text(
                        stringResource(id = R.string.add_device),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() })
                }
            }
        }*/

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0E1116).copy(alpha = 0.9f),
                            Color(0xFF0E1116)
                        ), startY = 0f, endY = 100f
                    )
                )
                .padding(bottom = dimensionResource(id = R.dimen.spacer_medium)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isTablet) 0.35f else 0.55f)
                        .widthIn(max = 300.dp)
                        .height(dimensionResource(id = R.dimen.button_height))
                        .shadow(
                            elevation = dimensionResource(id = R.dimen.elevation_large),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                            ambientColor = Color(0xFF77AEFF).copy(alpha = 0.5f),
                            spotColor = Color(0xFF77AEFF).copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                        .background(primaryGradient)
                        .clickable {
                            navController.navigate("add_device")
                        }, contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            stringResource(id = R.string.add_device),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() })
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsContent(navController: NavController, authViewModel: AuthViewModel) {
    val density = LocalDensity.current
    val textGrey = Color(0xFF9CA3AF)
    val cardBackground = Color(0xFF1B1F26)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var fullName by remember {
        mutableStateOf(
            prefs.getString("full_name", "Julian Sterling") ?: "Julian Sterling"
        )
    }
    var username by remember {
        mutableStateOf(
            prefs.getString("username", "julian.sterling") ?: "julian.sterling"
        )
    }
    val user = FirebaseAuth.getInstance().currentUser
    val userIdentifier = user?.email ?: user?.phoneNumber ?: "julian.sterling@nannyeye.io"
    var profileImageUri by remember { mutableStateOf(prefs.getString("profile_image", "")) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(fullName) }
    var updatedEmail by remember { mutableStateOf(user?.email ?: "") }
    var isVerificationSent by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Automatic fill data from Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val uname = document.getString("username") ?: ""
                        val pImage = document.getString("profile_image") ?: ""

                        if (name.isNotEmpty()) {
                            fullName = name
                            prefs.edit().putString("full_name", name).apply()
                        }
                        if (uname.isNotEmpty()) {
                            username = uname
                            prefs.edit().putString("username", uname).apply()
                        }
                        if (pImage.isNotEmpty()) {
                            profileImageUri = pImage
                            prefs.edit().putString("profile_image", pImage).apply()
                        }
                    }
                }
        }
    }

    // Function to upload image to Firebase Storage and get URL
    suspend fun uploadImageToFirebase(uri: Uri, identifier: String): String {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_images/$identifier.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val imageUrl = uploadImageToFirebase(it, userIdentifier)
                if (imageUrl.isNotEmpty()) {
                    profileImageUri = imageUrl
                    prefs.edit().putString("profile_image", imageUrl).apply()

                    // Update in Firestore
                    user?.uid?.let { uid ->
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(uid).update("profile_image", imageUrl)
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Firebase update failed: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }
    }

    val storageUsage = remember {
        try {
            val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
            val total = stat.totalBytes
            val free = stat.availableBytes
            val used = total - free
            if (total > 0) ((used * 100) / total).toInt() else 85
        } catch (e: Exception) {
            85
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.screen_padding))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Section from Image
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1C222B),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    if (!profileImageUri.isNullOrEmpty()) {
                        SubcomposeAsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF77AEFF),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(20.dp),
                                    tint = Color.White.copy(alpha = 0.4f)
                                )
                            })
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(20.dp),
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = 6.dp, y = 6.dp)
                        .clickable { galleryLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = Color(0xFFC5C6CD),
                    border = BorderStroke(2.dp, Color(0xFF0E1116))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp),
                        tint = Color(0xFF1B1F26)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fullName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    newName = fullName
                    showNameDialog = true
                })

            if (username.isNotEmpty()) {
                Text(
                    text = "($username)",
                    color = textGrey,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = userIdentifier,
                color = textGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable {
                        updatedEmail = user?.email ?: ""
                        isVerificationSent = false
                        showEmailDialog = true
                    })

            Spacer(modifier = Modifier.height(32.dp))

            if (showNameDialog) {
                AlertDialog(
                    onDismissRequest = { showNameDialog = false },
                    title = { Text("Update Name", color = Color.White) },
                    text = {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newName.isNotEmpty()) {
                                fullName = newName
                                prefs.edit().putString("full_name", newName).apply()

                                // Update in Firestore
                                user?.uid?.let { uid ->
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("users").document(uid).update("name", newName)
                                }

                                showNameDialog = false
                            }
                        }) {
                            Text("Save", color = Color(0xFF77AEFF))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNameDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1B1F26)
                )
            }

            if (showEmailDialog) {
                AlertDialog(
                    onDismissRequest = { showEmailDialog = false },
                    title = { Text("Update Email", color = Color.White) },
                    text = {
                        Column {
                            if (!isVerificationSent) {
                                OutlinedTextField(
                                    value = updatedEmail,
                                    onValueChange = { updatedEmail = it },
                                    label = { Text("New Email Address") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "We will send a verification link to this email.",
                                    color = textGrey,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    "Verification link sent to $updatedEmail. Please verify it in your inbox and then click 'Update'.",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (!isVerificationSent) {
                                if (updatedEmail.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(
                                        updatedEmail
                                    ).matches()
                                ) {
                                    scope.launch {
                                        try {
                                            // Using verifyBeforeUpdateEmail if supported
                                            user?.verifyBeforeUpdateEmail(updatedEmail)?.await()
                                            isVerificationSent = true
                                            Toast.makeText(
                                                context,
                                                "Verification email sent",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            val message = e.message ?: ""
                                            if (message.contains(
                                                    "recent-login", ignoreCase = true
                                                ) || message.contains(
                                                    "sensitive-operation", ignoreCase = true
                                                )
                                            ) {
                                                showReauthDialog = true
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context, "Invalid email address", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // User claims they verified
                                scope.launch {
                                    try {
                                        val auth = FirebaseAuth.getInstance()
                                        auth.currentUser?.reload()?.await()
                                        val currentUser = auth.currentUser

                                        // In verifyBeforeUpdateEmail, the user.email changes to updatedEmail ONLY AFTER verification
                                        if (currentUser?.email == updatedEmail) {
                                            // Update Firestore
                                            currentUser.uid.let { uid ->
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    .collection("users").document(uid)
                                                    .update("email", updatedEmail)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Email updated successfully in profile!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        showEmailDialog = false
                                                    }.addOnFailureListener { e ->
                                                        Toast.makeText(
                                                            context,
                                                            "Firestore update failed: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Please verify your email address first (Check: ${currentUser?.email})",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context, "Error: ${e.message}", Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }) {
                            Text(
                                if (isVerificationSent) "Update" else "Send Link",
                                color = Color(0xFF77AEFF)
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEmailDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1B1F26)
                )
            }

            if (showReauthDialog) {
                AlertDialog(
                    onDismissRequest = { showReauthDialog = false },
                    title = { Text("Confirm Identity", color = Color.White) },
                    text = {
                        Column {
                            Text(
                                "Please enter your current password to proceed with the email update.",
                                color = textGrey,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = reauthPassword,
                                onValueChange = { reauthPassword = it },
                                label = { Text("Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (reauthPassword.isNotEmpty()) {
                                scope.launch {
                                    try {
                                        val credential =
                                            com.google.firebase.auth.EmailAuthProvider.getCredential(
                                                user?.email ?: "", reauthPassword
                                            )
                                        user?.reauthenticate(credential)?.await()
                                        showReauthDialog = false
                                        reauthPassword = ""
                                        // Retry the update
                                        user?.verifyBeforeUpdateEmail(updatedEmail)?.await()
                                        isVerificationSent = true
                                        Toast.makeText(
                                            context,
                                            "Re-authenticated! Verification email sent.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Re-authentication failed: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }) {
                            Text("Verify", color = Color(0xFF77AEFF))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReauthDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1B1F26)
                )
            }

            // Account Security Card (matching image style)
            SettingsItem(
                Icons.Outlined.Security,
                "Account Security",
                "2FA enabled, Password updated 30d ago",
                onClick = {})

            // Subscription Plan Card (PRO)
            SettingsItem(
                Icons.Outlined.CardMembership,
                "Subscription Plan",
                "Cloud Storage (30 days) • 4K AI Analytics",
                proBadge = true,
                onClick = {})

            Spacer(modifier = Modifier.height(16.dp))

            // Linked Devices Section (matching image)
            LinkedDevicesSection(navController)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.preferences_security),
                color = textGrey,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

            SettingsItem(
                Icons.Outlined.Notifications,
                stringResource(id = R.string.notifications),
                "Motion alerts, System health, Daily recaps",
                onClick = {
                    val intent = Intent()
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        intent.action = Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", context.packageName)
                        intent.putExtra("app_uid", context.applicationInfo.uid)
                    }
                    context.startActivity(intent)
                },
            )

            val currentLang = LocaleHelper.getLanguage(context) ?: "en"
            val langName = when (currentLang) {
                "hi" -> stringResource(id = R.string.hindi)
                "gu" -> stringResource(id = R.string.gujarati)
                "es" -> stringResource(id = R.string.spanish)
                "fr" -> stringResource(id = R.string.french)
                "de" -> stringResource(id = R.string.german)
                "it" -> stringResource(id = R.string.italian)
                "pt" -> stringResource(id = R.string.portuguese)
                "ru" -> stringResource(id = R.string.russian)
                "zh" -> stringResource(id = R.string.chinese_simplified)
                "ja" -> stringResource(id = R.string.japanese)
                "ko" -> stringResource(id = R.string.korean)
                "ar" -> stringResource(id = R.string.arabic)
                "tr" -> stringResource(id = R.string.turkish)
                "nl" -> stringResource(id = R.string.dutch)
                "pl" -> stringResource(id = R.string.polish)
                "sv" -> stringResource(id = R.string.swedish)
                "no" -> stringResource(id = R.string.norwegian)
                "da" -> stringResource(id = R.string.danish)
                "fi" -> stringResource(id = R.string.finnish)
                "el" -> stringResource(id = R.string.greek)
                "iw" -> stringResource(id = R.string.hebrew)
                "th" -> stringResource(id = R.string.thai)
                "vi" -> stringResource(id = R.string.vietnamese)
                "id" -> stringResource(id = R.string.indonesian)
                "ms" -> stringResource(id = R.string.malay)
                "cs" -> stringResource(id = R.string.czech)
                "hu" -> stringResource(id = R.string.hungarian)
                "ro" -> stringResource(id = R.string.romanian)
                "sk" -> stringResource(id = R.string.slovak)
                "uk" -> stringResource(id = R.string.ukrainian)
                "zh-rTW" -> stringResource(id = R.string.chinese_traditional)
                else -> stringResource(id = R.string.english)
            }

            SettingsItem(
                Icons.Outlined.Language,
                stringResource(id = R.string.language),
                langName,
                onClick = {
                    navController.navigate("language_selection")
                },
            )
            SettingsItem(
                Icons.Outlined.Storage,
                stringResource(id = R.string.manage_storage),
                stringResource(id = R.string.storage_full_label, "$storageUsage%"),
                showChevron = true,
                subColor = if (storageUsage > 80) Color(0xFFE57373) else textGrey,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_MEMORY_CARD_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
                            context.startActivity(intent)
                        } catch (ex: Exception) {
                            Toast.makeText(
                                context, "Could not open storage settings", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
            Text(
                text = stringResource(id = R.string.support_information),
                color = textGrey,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

            SettingsItem(
                Icons.Outlined.HelpOutline,
                stringResource(id = R.string.how_to_use),
                onClick = {
                    navController.navigate("sentinel_guide")
                },
            )
            SettingsItem(
                Icons.Outlined.PrivacyTip,
                stringResource(id = R.string.privacy_policy),
                onClick = {
                    navController.navigate("privacy_policy")
                },
            )
            SettingsItem(
                Icons.Outlined.ErrorOutline,
                stringResource(id = R.string.report_problem),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:info@hacksec.ai")
                    }
                    context.startActivity(intent)
                },
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_large)))

            Button(
                onClick = {
                    navController.navigate("sign_out_confirmation")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D1619), // Dark reddish background as per image
                    contentColor = Color(0xFFFF8A80) // Reddish text/icon color
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.sign_out),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_large)))
        }

    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    hasSwitch: Boolean = false,
    hasExternalLink: Boolean = false,
    proBadge: Boolean = false,
    hasDot: Boolean = false,
    subColor: Color = Color(0XFFC5C6CD),
    onClick: () -> Unit = {},
    showChevron: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacer_small))
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C22))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.card_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.icon_size_large))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)))
                    .background(Color(0XFF31353C).copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0XFFBBC6E2).copy(alpha = 0.7f),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard))
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0XFFDFE2EB),
                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_body).toSp() })
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = subColor,
                        fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_caption).toSp() })
                }
            }

            if (proBadge) {
                Surface(
                    color = Color(0xFFC5C6CD).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "PRO",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF).copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CustomPermissionDialog(onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.screen_padding_small))
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.screen_padding))
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.screen_padding))
                            .clickable { onDismiss() })
                }

                Text(
                    text = stringResource(id = R.string.permission_title),
                    color = Color.White,
                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_title).toSp() },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                Text(
                    text = stringResource(id = R.string.permission_desc),
                    color = Color.LightGray,
                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_small).toSp() },
                    lineHeight = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_title).toSp() })

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF2C2C2C),
                            RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                        )
                        .padding(dimensionResource(id = R.dimen.card_padding))
                ) {
                    PermissionStep(
                        icon = Icons.Default.Settings,
                        text = stringResource(id = R.string.permission_step1)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
                    PermissionStep(
                        icon = Icons.Default.Videocam,
                        text = stringResource(id = R.string.permission_step2)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
                    PermissionStep(
                        icon = Icons.Default.Adjust,
                        text = stringResource(id = R.string.permission_step3)
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.open_settings),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_body).toSp() })
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionStep(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(
                    dimensionResource(id = R.dimen.icon_size_medium) - dimensionResource(
                        id = R.dimen.spacer_small
                    ) / 2
                )
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard) / 2)
            )
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
        Text(
            text = text,
            color = Color.White,
            fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_caption).toSp() },
            lineHeight = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_subtitle).toSp() })
    }
}

@Composable
fun LinkedDevicesSection(
    navController: NavController,
    viewModel: LinkedDevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1B1F26))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Linked Devices",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Manage All",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("linked_devices_screen")
                })
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(devices) { device ->
                LinkedDeviceCard(device)
            }
        }
    }
}

@Composable
fun LinkedDeviceCard(device: LinkedDevice) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242B33))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1B1F26).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.platform == "Web" || device.platform == "PC") Icons.Default.Laptop else Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = device.deviceName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = if (device.isCurrent) "CURRENT DEVICE" else "ACTIVE ${
                        formatTimestamp(
                            device.lastActive
                        )
                    }",
                    color = if (device.isCurrent) Color(0xFF77AEFF) else Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedDevicesScreen(
    navController: NavController,
    viewModel: LinkedDevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        "Linked Devices",
                        color = Color.White
                    )
                }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                }, colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E1116)
                )
            )
        }, containerColor = Color(0xFF0E1116)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Your account is logged in on these devices. You can log out from any device remotely.",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices) { device ->
                    LinkedDeviceListItem(
                        device = device, onLogout = { viewModel.logoutDevice(device.deviceId) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.logoutAllExceptCurrent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D1619), contentColor = Color(0xFFFF8A80)
                )
            ) {
                Text("Logout All Other Devices", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LinkedDeviceListItem(device: LinkedDevice, onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F26))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (device.platform == "Web" || device.platform == "PC") Icons.Default.Laptop else Icons.Default.Smartphone,
                contentDescription = null,
                tint = Color(0xFF77AEFF),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${device.platform} • ${
                        if (device.isCurrent) "Current Device" else "Last active " + formatTimestamp(
                            device.lastActive
                        )
                    }", color = Color(0xFF9CA3AF), fontSize = 12.sp
                )
                val displayStatus = remember(device) {
                    if (device.isCurrent) "Online"
                    else if (device.status == "Online") {
                        val lastActiveTime = device.lastActive?.toDate()?.time ?: 0
                        val diff = System.currentTimeMillis() - lastActiveTime
                        if (diff > 120000) "Offline" else "Online" // If no heartbeat for 2 mins, show Offline
                    } else "Offline"
                }

                Text(
                    text = displayStatus,
                    color = if (displayStatus == "Online") Color.Green else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF242B33))
                ) {
                    DropdownMenuItem(text = {
                        Text(
                            if (device.isCurrent) "Logout Current Session" else "Logout Device",
                            color = Color.Red
                        )
                    }, onClick = {
                        showMenu = false
                        onLogout()
                    })
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown"
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    val seconds = diff / 1000
    if (seconds < 60) return "Just now"
    val minutes = seconds / 60
    if (minutes < 60) return "${minutes}m ago"
    val hours = minutes / 60
    if (hours < 24) return "${hours}h ago"
    val days = hours / 24
    if (days < 7) return "${days}d ago"
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@Composable
fun AddCameraDialog(
    onDismiss: () -> Unit,
    onManualCode: () -> Unit,
    onConnectIPCamera: () -> Unit,
    onScanSuccess: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(32.dp)),
            color = Color(0xFF0E1116)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Add New Camera",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Position the QR code located on the\nbottom of your camera within the\nscanner frame.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Scanner Frame
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
                    var flashEnabled by remember { mutableStateOf(false) }

                    CameraScannerPreview(
                        onScanSuccess = onScanSuccess,
                        onCameraReady = { cameraControl = it }
                    )

                    // Frame Overlay
                    ScannerFrameOverlay()

                    // Scanning indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ScanningDot()
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SCANNING...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = {
                                    flashEnabled = !flashEnabled
                                    cameraControl?.enableTorch(flashEnabled)
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flash",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Buttons
                Button(
                    onClick = onManualCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Add the code manually", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConnectIPCamera,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Router, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Connect IP camera", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "TROUBLE CONNECTING? ASK IRIS",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun CameraScannerPreview(onScanSuccess: (String) -> Unit, onCameraReady: (CameraControl) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasScanned by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                @androidx.camera.core.ExperimentalGetImage
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    if (hasScanned) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val value = barcode.rawValue
                                    if (value != null && !hasScanned) {
                                        hasScanned = true
                                        try {
                                            val json = JSONObject(value)
                                            val scannedSessionId = json.getString("session_id")
                                            onScanSuccess(scannedSessionId)
                                        } catch (e: Exception) {
                                            onScanSuccess(value)
                                        }
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    onCameraReady(camera.cameraControl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ScannerFrameOverlay() {
    val strokeWidth = 4.dp
    val cornerSize = 40.dp
    val radius = 12.dp

    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        val strokeWidthPx = strokeWidth.toPx()
        val cornerSizePx = cornerSize.toPx()
        val radiusPx = radius.toPx()

        // Top Left
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, cornerSizePx)
                lineTo(0f, radiusPx)
                quadraticTo(0f, 0f, radiusPx, 0f)
                lineTo(cornerSizePx, 0f)
            },
            color = Color(0xFFBBC6E2),
            style = Stroke(strokeWidthPx)
        )

        // Top Right
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width - cornerSizePx, 0f)
                lineTo(size.width - radiusPx, 0f)
                quadraticTo(size.width, 0f, size.width, radiusPx)
                lineTo(size.width, cornerSizePx)
            },
            color = Color(0xFFBBC6E2),
            style = Stroke(strokeWidthPx)
        )

        // Bottom Left
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, size.height - cornerSizePx)
                lineTo(0f, size.height - radiusPx)
                quadraticTo(0f, size.height, radiusPx, size.height)
                lineTo(cornerSizePx, size.height)
            },
            color = Color(0xFFBBC6E2),
            style = Stroke(strokeWidthPx)
        )

        // Bottom Right
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width - cornerSizePx, size.height)
                lineTo(size.width - radiusPx, size.height)
                quadraticTo(size.width, size.height, size.width, size.height - radiusPx)
                lineTo(size.width, size.height - cornerSizePx)
            },
            color = Color(0xFFBBC6E2),
            style = Stroke(strokeWidthPx)
        )
    }
}

@Composable
fun ScanningDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Surface(
        modifier = Modifier.size(8.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = alpha)
    ) {}
}

@Composable
fun ManualPairingDialog(
    onDismiss: () -> Unit,
    onConnect: (String) -> Unit
) {
    var pairingCode by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add the code manually", color = Color.White) },
        text = {
            Column {
                Text("Enter the 9 or 10-digit code shown on the camera device.", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = { pairingCode = it },
                    label = { Text("Pairing Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF77AEFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pairingCode.isNotEmpty()) onConnect(pairingCode) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF77AEFF))
            ) {
                Text("Connect", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1B1F26)
    )
}

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val navBackground = Color(0xFF0E1116)
    val selectedContainerColor = Color(0xFF1B263B)
    val selectedContentColor = Color(0xFF77AEFF)
    val unselectedContentColor = Color(0xFF4B5563)
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val navHeight = if (isTablet) 88.dp else 64.dp
    val iconSize = if (isTablet) 24.dp else 20.dp
    val textSize = if (isTablet) 11.sp else 9.sp

    val items = listOf(
        stringResource(id = R.string.dashboard),
        stringResource(id = R.string.records),
        stringResource(id = R.string.devices),
        stringResource(id = R.string.settings)
    )
    val icons = listOf(
        ImageVector.vectorResource(id = R.drawable.gridview),
        ImageVector.vectorResource(id = R.drawable.folder),
        ImageVector.vectorResource(id = R.drawable.videocam),
        ImageVector.vectorResource(id = R.drawable.setting_icon)
    )

    Surface(
        color = navBackground, modifier = Modifier
            .fillMaxWidth()
            .height(navHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTablet) 12.dp else 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index
                val contentColor = if (isSelected) selectedContentColor else unselectedContentColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            vertical = if (isTablet) 12.dp else 8.dp,
                            horizontal = if (isTablet) 8.dp else 4.dp
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) selectedContainerColor else Color.Transparent)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) }
                        .padding(vertical = if (isTablet) 8.dp else 4.dp),
                    contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            tint = contentColor,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.height(if (isTablet) 4.dp else 2.dp))
                        Text(
                            text = item,
                            color = contentColor,
                            fontSize = textSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
