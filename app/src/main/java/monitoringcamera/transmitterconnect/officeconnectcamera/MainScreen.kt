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
                0 -> DashboardContent(navController, viewModel)
                1 -> RecordsContent(navController)
                2 -> DevicesContent(navController, viewModel)
                3 -> SettingsContent(navController, authViewModel)
            }
        }
    }
}

@Composable
fun DashboardContent(navController: NavController, viewModel: CameraViewModel) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = configuration.screenWidthDp >= 600
    val cardBackground = Color(0xFF1B1F26)
    val primaryGradient =
        Brush.horizontalGradient(colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B)))
    val textGrey = Color(0xFF9CA3AF)

    val activeSessions = remember { mutableStateListOf<String>() }
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())
    val roomDevices by viewModel.roomDevices.observeAsState(emptyList())
    val mySessionId by viewModel.sessionId.observeAsState("")
    val isCreator by viewModel.isCreator.observeAsState(false)
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())
    val myCreatedSessions by viewModel.myCreatedSessions.observeAsState(emptyList())
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val myDeviceId by viewModel.myDeviceId.observeAsState("")

    var selectedSessionIdForViewers by remember { mutableStateOf<String?>(null) }
    var expandedSessionId by remember { mutableStateOf<String?>(null) }
    var expandedDeviceId by remember { mutableStateOf<String?>(null) }
    var expandedIPDeviceId by remember { mutableStateOf<String?>(null) }
    var showBusyDialog by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }
    var selectedSessionIdForConnection by remember { mutableStateOf<String?>(null) }

    val isConnected by viewModel.isConnected.observeAsState(false)

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(selectedSessionIdForViewers) {
        selectedSessionIdForViewers?.let { viewModel.listenForViewers(it) }
    }

    // Automated trigger for both Roles: Listen for connection requests from others
    LaunchedEffect(myDeviceId, isConnected) {
        if (myDeviceId.isNotEmpty() && !isConnected) {
            val database = FirebaseDatabase.getInstance()
            val requestRef = database.getReference("SaveSessions").child(myDeviceId)
            requestRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // A peer has initiated a connection request to this device ID
                        val sessionId = snapshot.children.firstOrNull()?.key
                        val isSessionAvailable = myCreatedSessions.any { it.sessionId == sessionId }

                        if (isSessionAvailable) {
                            if (sessionId != null && !showConnectDialog && !isConnected) {
                                selectedSessionIdForConnection = sessionId
                                showConnectDialog = true
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.screen_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display current device's session only if it's the creator device
                if (isCreator) {
                    if (myCreatedSessions.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.recent_sessions),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        myCreatedSessions.forEach { session ->
                            val isLive = activeSessions.contains(session.sessionId)
                            val isExpanded = expandedSessionId == session.sessionId
                            RecentSessionItem(
                                session = session,
                                isLive = isLive,
                                isExpanded = isExpanded,
                                onExpandToggle = {
                                    expandedSessionId = if (isExpanded) null else session.sessionId
                                    expandedDeviceId = null
                                    expandedIPDeviceId = null
                                    viewModel.sessionId.value = session.sessionId
                                },
                                onConnectLive = {
                                    selectedSessionIdForConnection = session.sessionId
                                    showConnectDialog = true
                                    Log.e("check8951", "showConnectDialog2")
                                },
                                navController = navController,
                                viewModel = viewModel,
                                onRemove = { viewModel.deleteSessionRecord(session.sessionId) })
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
                    }
                }

                // Placeholder Area - Show when no devices are paired and no sessions are active
                if (myCreatedSessions.isEmpty() && savedDevices.isEmpty() && roomDevices.isEmpty()) {
                    Image(
                        painter = painterResource(id = R.drawable.empty_state),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(
                                if (isTablet) screenWidthDp * 0.4f
                                else screenWidthDp * 0.75f
                            )
                            .widthIn(max = 500.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                    Text(
                        text = stringResource(id = R.string.no_cameras_added),
                        color = Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_h2).toSp() },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                    Text(
                        text = stringResource(id = R.string.no_cameras_description),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        lineHeight = with(density) { dimensionResource(id = R.dimen.text_title).toSp() },
                        textAlign = TextAlign.Center
                    )
                }

                if (savedDevices.isNotEmpty() || roomDevices.isNotEmpty()) {
                    // Group by deviceId and keep the most recent session for each device
                    // This prevents duplicate entries and ensures correct ID matching.
                    val latestSavedDevices = savedDevices.groupBy { it.deviceId }
                        .map { entry -> entry.value.maxByOrNull { it.timestamp }!! }
                        .sortedByDescending { it.timestamp }

                    val otherSavedDevices = latestSavedDevices.filter { device ->
                        if (device.role == "monitor") {
                            !myCreatedSessions.any { it.sessionId == device.sessionId }
                        } else {
                            device.sessionId != mySessionId
                        }
                    }
                    if (otherSavedDevices.isNotEmpty() || roomDevices.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.connected_cameras),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(id = R.dimen.spacer_small))
                        )
                        otherSavedDevices.forEach { device ->
                            val onlineInfo = onlineMonitors[device.deviceId]
                            // Strict matching: Only show as "Online" if the current active session on the device
                            // matches the specific 10-digit pairing code (sessionId) we have saved.
                            val isLive =
                                onlineInfo != null && onlineInfo.sessionId == device.sessionId

                            val isExpanded = expandedDeviceId == device.deviceId

                            StreamItem(
                                sessionId = device.sessionId,
                                deviceId = device.deviceId,
                                name = device.name,
                                isActive = isLive,
                                isExpanded = isExpanded,
                                onExpandToggle = {
                                    expandedDeviceId = if (isExpanded) null else device.deviceId
                                    expandedSessionId = null
                                    expandedIPDeviceId = null
                                },
                                isOccupied = if (isLive) onlineInfo.isOccupied else false,
                                navController = navController,
                                role = device.role,
                                cameraViewModel = viewModel,
                                onRemove = { viewModel.deleteSavedDevice(device.deviceId) })
                        }

                        roomDevices.forEach { device ->
                            val isExpanded = expandedIPDeviceId == device.id
                            IPCameraStreamItem(
                                device = device,
                                isExpanded = isExpanded,
                                onExpandToggle = {
                                    expandedIPDeviceId = if (isExpanded) null else device.id
                                    expandedSessionId = null
                                    expandedDeviceId = null
                                },
                                navController = navController,
                                onRemove = { viewModel.deleteRoomDevice(device) })
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(id = R.dimen.section_spacing).times(
                            3
                        )
                    )
                )
            }
        }

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
                            /*if (AdsDataHolder.adsData != null) {
                                FacebookAds.getInstance(activity).ShowInterstitial(
                                    activity,
                                    AdsDataHolder.adsData.checkAdPrivacyPolicyInter,
                                    AdsDataHolder.adsData.fbinter1,
                                    AdsDataHolder.adsData.qurekaInterImgUrl1,
                                    object : MyCallback {
                                        override fun onCall() {
                                            navController.navigate("add_device")
                                        }
                                    })
                            } else {
                                navController.navigate("add_device")
                            }*/
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
                            stringResource(id = R.string.add_camera),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() })
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                Row(modifier = Modifier
                    .clickable { navController.navigate("sentinel_guide") }
                    .padding(dimensionResource(id = R.dimen.spacer_small)),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                    Text(
                        text = stringResource(id = R.string.need_help),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() })
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_micro)))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_tiny))
                    )
                }
            }
        }
    }

    if (selectedSessionIdForViewers != null) {
        AlertDialog(
            onDismissRequest = { selectedSessionIdForViewers = null },
            title = {
                Text(
                    stringResource(
                        id = R.string.session_label, selectedSessionIdForViewers!!
                    )
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(id = R.string.connected_devices),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacer_small))
                    )
                    if (connectedViewers.isEmpty()) {
                        Text(
                            stringResource(id = R.string.no_devices_connected_label),
                            color = textGrey,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() })
                    } else {
                        connectedViewers.forEach { viewer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = dimensionResource(id = R.dimen.spacer_micro)),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewer.name,
                                        color = Color.White,
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(
                                            id = R.string.id_label, viewer.deviceId
                                        ),
                                        color = textGrey,
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() })
                                }
                                Text(
                                    text = if (viewer.status == "Online") stringResource(id = R.string.online) else viewer.status,
                                    color = if (viewer.status == "Online") Color(0xFF4CAF50) else Color.Red,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSessionIdForViewers = null }) {
                    Text(stringResource(id = R.string.close), color = Color(0xFF77AEFF))
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
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

    if (showConnectDialog && selectedSessionIdForConnection != null) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.device_online_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(id = R.string.connect_request_message))
            },
            confirmButton = {
                TextButton(onClick = {
                    showConnectDialog = false
                    val sid = selectedSessionIdForConnection!!

                    val session = myCreatedSessions.find { it.sessionId == sid }
                    val connectedDeviceId =
                        session?.connectedDevices?.getOrNull(0)?.split(":")?.getOrNull(0) ?: ""

                    val database = FirebaseDatabase.getInstance()
                    val ref = database.getReference("SaveSessions")
                        .child(connectedDeviceId.ifEmpty { "unknown" })
                        .child(session?.sessionId.toString())

                    val saveData = mapOf("status" to "Online")
                    ref.setValue(saveData)
                    ref.onDisconnect().removeValue()

                    // Determine if we should act as Viewer or Monitor
                    val isMyCreatedSession = myCreatedSessions.any { it.sessionId == sid }
                    val isLive = activeSessions.contains(sid)

                    if (isMyCreatedSession) {
                        // We are the Monitor for this session
                        if (!isLive) {
                            viewModel.resumeHostSession(sid)
                        }
                        navController.navigate("camera_view/${Uri.encode(sid)}")
                    } else {
                        // We are a Viewer for this session
                        navController.navigate("viewer/${Uri.encode(sid)}")
                    }
                }) {
                    Text(stringResource(id = R.string.accept), color = Color(0xFF77AEFF))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConnectDialog = false
                    viewModel.declineRemoteSession(selectedSessionIdForConnection ?: "", myDeviceId)
                }) {
                    Text(stringResource(id = R.string.cancel), color = Color.Red)
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
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
                                                    context.getString(R.string.could_not_open_video),
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
