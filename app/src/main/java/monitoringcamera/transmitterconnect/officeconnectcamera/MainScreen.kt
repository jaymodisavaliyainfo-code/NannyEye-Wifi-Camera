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
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showDot: Boolean = false,
    textColor: Color = Color.White,
    iconColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF161B22), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            if (showDot) {
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF8A65)
                ) {}
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title.uppercase(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsDrawerContent(
    navController: NavController,
    authViewModel: AuthViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.user.collectAsState()
    val fullName = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("full_name", user?.displayName ?: "User") ?: "User"
    }
    val username = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("username", fullName.lowercase().replace(" ", "")) ?: ""
    }
    val profileImageUri = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("profile_image", "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1116))
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1C222B)
                ) {
                    if (!profileImageUri.isNullOrEmpty()) {
                        SubcomposeAsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 4.dp, y = 4.dp),
                    shape = CircleShape,
                    color = Color(0xFFBBC6E2),
                    border = BorderStroke(2.dp, Color(0xFF0E1116))
                ) {}
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .rotate(180f)
                        .size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Good afternoon, $fullName",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "🛡 @${username.uppercase()} • SURVEILLANCE HUB",
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "Account",
                onClick = {
                    onClose()
                    navController.navigate("account_settings")
                }
            )
            DrawerMenuItem(
                icon = Icons.Default.Tune,
                title = "Preferences",
                onClick = {
                    onClose()
                    navController.navigate("preferences")
                }
            )
            DrawerMenuItem(
                icon = Icons.Outlined.Notifications,
                title = "Notification",
                showDot = true,
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
                }
            )
            DrawerMenuItem(
                icon = Icons.Outlined.Language,
                title = "Language",
                onClick = { navController.navigate("language_selection") }
            )
            DrawerMenuItem(
                icon = Icons.Outlined.PrivacyTip,
                title = "Privacy Policy",
                onClick = { navController.navigate("privacy_policy") }
            )
            DrawerMenuItem(
                icon = Icons.Outlined.ErrorOutline,
                title = "Report a problem",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:info@hacksec.ai")
                    }
                    context.startActivity(intent)
                }
            )
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = "Contact Us",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:info@hacksec.ai")
                    }
                    context.startActivity(intent)
                }
            )
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign Out",
                textColor = Color(0xFFFF8A80),
                iconColor = Color(0xFFFF8A80),
                onClick = { navController.navigate("sign_out_confirmation") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NANNYEYE OS • V2.4.1",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    linkedDevicesViewModel: LinkedDevicesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkBackground = Color(0xFF0E1116)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showQrDialog by remember { mutableStateOf(false) }
    val isConnected by viewModel.getSessionConnectionState(viewModel.sessionId.observeAsState("").value).observeAsState(false)
    val reconnectRequest by viewModel.incomingReconnectRequest.observeAsState()

    // Handle Reconnect Request dialog
    reconnectRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.declineReconnect() },
            title = { Text("Reconnect Monitor", color = Color.White) },
            text = {
                Text(
                    "${request["hostName"]} wants to reconnect to the previous monitoring session.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(onClick = {
                    val sId = request["sessionId"] ?: ""
                    val hostName = request["hostName"] ?: "Remote Camera"
                    val requestId = request["requestId"] ?: ""
                    if (requestId.isNotEmpty()) {
                        viewModel.acceptFirestoreReconnectRequest(requestId)
                    }
                    // Pre-populate the device name map for concurrent viewing
                    val currentNames = viewModel.activePreviewDeviceNames.value ?: emptyMap()
                    viewModel.activePreviewDeviceNames.value = currentNames + (sId to hostName)

                    viewModel.startViewing(sId)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val requestId = request["requestId"] ?: ""
                    if (requestId.isNotEmpty()) {
                        viewModel.declineFirestoreReconnectRequest(requestId)
                    } else {
                        viewModel.declineReconnect()
                    }
                }) {
                    Text("Decline", color = Color.Red)
                }
            },
            containerColor = Color(0xFF1B1F26),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

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

    LaunchedEffect(Unit) {
        linkedDevicesViewModel.updateActivity()
    }

    // Force portrait orientation when on MainScreen
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {}
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0E1116),
                drawerTonalElevation = 0.dp,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                SettingsDrawerContent(
                    navController = navController,
                    authViewModel = authViewModel,
                    onClose = { coroutineScope.launch { drawerState.close() } }
                )
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            containerColor = darkBackground, bottomBar = {
                BottomNavigationBar(pagerState.currentPage, viewModel) { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            }

        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding),
                userScrollEnabled = true,
                beyondViewportPageCount = 3
            ) { page ->
                when (page) {
                    0 -> DashboardContent(
                        navController,
                        viewModel,
                        authViewModel,
                        linkedDevicesViewModel,
                        onProfileClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    1 -> DevicesContent(navController, viewModel, onNavigateToDashboard = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    })
                    2 -> PremiumContent()
                    3 -> RecordsContent(navController)
                    4 -> AlertsContent(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        onProfileClick = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Premium Content Coming Soon", color = Color.White)
    }
}

@Composable
fun AlertsContent(
    viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val textGrey = Color(0xFF9CA3AF)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "PEOPLE", "PETS", "VEHICLE", "MOTION", "SYSTEM")

    val user by authViewModel.user.collectAsState()
    val fullName = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("full_name", user?.displayName ?: "User") ?: "User"
    }
    val profileImageUri = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("profile_image", "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onProfileClick() },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1C222B)
            ) {
                if (!profileImageUri.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "NannyEye",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NO NEW ALERTS",
                            color = textGrey,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    "Search alerts, cameras, or events...",
                    color = textGrey,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    null,
                    tint = textGrey,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Tune,
                    null,
                    tint = textGrey,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161B22),
                unfocusedContainerColor = Color(0xFF161B22),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                AlertFilterChip(
                    text = filter,
                    count = 0,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AlertFilterChip(text: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(36.dp),
        color = if (isSelected) Color(0xFF1B263B) else Color(0xFF161B22),
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFF77AEFF) else Color(0xFF9CA3AF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = (if (isSelected) Color(0xFF77AEFF) else Color(0xFF9CA3AF)).copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = count.toString(),
                        color = if (isSelected) Color(0xFF77AEFF) else Color(0xFF9CA3AF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    navController: NavController,
    viewModel: CameraViewModel,
    authViewModel: AuthViewModel,
    linkedDevicesViewModel: LinkedDevicesViewModel,
    onProfileClick: () -> Unit
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

    val profileImageUri = remember(user) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.getString("profile_image", "")
    }

    val activeSessions = remember { mutableStateListOf<String>() }
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())
    val videoRecords by viewModel.videoRecords.observeAsState(emptyList())
    val savedViewers by viewModel.savedViewers.observeAsState(emptyList())
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val myDeviceId by viewModel.myDeviceId.observeAsState("")
    val sessionId by viewModel.sessionId.observeAsState("")
    val isConnected by viewModel.getSessionConnectionState(sessionId).observeAsState(false)
    val isBroadcasting by viewModel.isBroadcasting.observeAsState(false)
    val activePreviewSessions by viewModel.activePreviewSessionIds.observeAsState(emptySet())
    val activePreviewDeviceNames by viewModel.activePreviewDeviceNames.observeAsState(emptyMap())
    
    val qrBitmap by viewModel.qrBitmap.observeAsState()
    val cameraActivities by viewModel.cameraActivities.observeAsState(emptyList())
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())

    var showQrDialog by remember { mutableStateOf(false) }
    var showAddCameraDialog by remember { mutableStateOf(false) }
    var showManualPairingDialog by remember { mutableStateOf(false) }
    var showIPCameraSetupDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    var pendingAction by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            when (pendingAction) {
                "start_broadcasting" -> {
                    viewModel.generateHostSession()
                    showQrDialog = true
                }

                "add_camera" -> {
                    showAddCameraDialog = true
                }
            }
            pendingAction = null
        } else {
            val cameraGranted = results[Manifest.permission.CAMERA] ?: false
            val audioGranted = results[Manifest.permission.RECORD_AUDIO] ?: false

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
            pendingAction = null
        }
    }

    val checkAndRequestPermissions: (String) -> Unit = { action ->
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            when (action) {
                "start_broadcasting" -> {
                    viewModel.generateHostSession()
                    showQrDialog = true
                }

                "add_camera" -> {
                    showAddCameraDialog = true
                }
            }
        } else {
            pendingAction = action
            permissionLauncher.launch(permissions)
        }
    }

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
    // Removed LaunchedEffect(isRemoteConnected) because we now support multiple independent sessions.
    // Dialog is closed directly when session is registered.

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
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onProfileClick() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C222B)
            ) {
                if (!profileImageUri.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
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
            val onlineCount = if (isBroadcasting) {
                connectedViewers.size
            } else {
                savedDevices.count { device ->
                    val onlineInfo = onlineMonitors[device.deviceId]
                    onlineInfo != null && onlineInfo.sessionId == device.sessionId
                }
            }

            StatCard(
                title = if (isBroadcasting) "VIEWERS\nONLINE" else "CAMERAS\nONLINE",
                count = onlineCount.toString(),
                subtitle = if (isBroadcasting) {
                    if (onlineCount == 0) "No one watching" else "$onlineCount active"
                } else {
                    if (onlineCount == 0) "No streams active" else "$onlineCount active"
                },
                icon = if (isBroadcasting) Icons.Default.Person else Icons.Default.Videocam,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ALERTS\nTODAY",
                count = "Count",
                subtitle = "Detection events",
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
                    Text(
                        "MANAGE",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
                modifier = Modifier.clickable { navController.navigate("monitor_wall") }
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
                val isBroadcastingCard = isBroadcasting || sessionId.isNotEmpty()
                ThisDeviceCard(
                    isBroadcasting = isBroadcastingCard,
                    isConnected = isConnected,
                    sessionId = sessionId,
                    viewModel = viewModel,
                    onStartCamera = {
                        checkAndRequestPermissions("start_broadcasting")
                    },
                    onCardClick = {
                        if (isConnected) {
                            navController.navigate("camera_view/${Uri.encode(sessionId)}")
                        }
                    }
                )
            }
            activePreviewSessions.forEach { sid ->
                val previewName = activePreviewDeviceNames[sid] ?: "Remote Camera"
                item {
                    RemoteDeviceCard(
                        sessionId = sid,
                        deviceName = previewName,
                        viewModel = viewModel,
                        onClose = {
                            viewModel.stopRemotePreview(sid)
                        },
                        onClick = {
                            navController.navigate("viewer/${Uri.encode(sid)}")
                        }
                    )
                }
            }
            item {
                AddCameraCard(
                    onClick = {
                        checkAndRequestPermissions("add_camera")
                    }
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
                modifier = Modifier
                    .size(20.dp)
                    .clickable { navController.navigate("all_devices_screen") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F25))
        ) {
            val combinedRecent = remember(savedDevices, savedViewers) {
                val data = mutableListOf<Map<String, Any>>()
                savedDevices.forEach {
                    data.add(mapOf("id" to it.deviceId, "name" to it.name, "time" to it.timestamp, "role" to it.role, "sid" to it.sessionId))
                }
                savedViewers.forEach {
                    data.add(mapOf("id" to it.deviceId, "name" to it.name, "time" to it.timestamp, "role" to "viewer", "sid" to it.sessionId))
                }
                data.sortedByDescending { it["time"] as Long }.take(3)
            }

            if (combinedRecent.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No remote devices added yet",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                combinedRecent.forEach { deviceMap ->
                    val dId = deviceMap["id"] as String
                    val name = deviceMap["name"] as String
                    val timestamp = deviceMap["time"] as Long
                    val role = deviceMap["role"] as String
                    val sid = deviceMap["sid"] as String

                    val isLive = if (role == "viewer") {
                        connectedViewers.any { it.deviceId == dId }
                    } else {
                        onlineMonitors[dId]?.sessionId == sid
                    }

                    DeviceRowItem(
                        name = name,
                        timestamp = timestamp,
                        status = if (isLive) "ONLINE" else "OFFLINE",
                        onActivate = {
                            if (isLive) {
                                if (role == "viewer") {
                                    navController.navigate("camera_view/${Uri.encode(sid)}")
                                } else {
                                    navController.navigate("viewer/${Uri.encode(sid)}")
                                }
                            }
                        }
                    )
                }
            }
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
                modifier = Modifier.clickable { navController.navigate("all_activities_screen") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cameraActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent activity",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            val targetRole = remember(sessionId, isBroadcasting, cameraActivities) {
                if (sessionId.isNotEmpty()) {
                    if (isBroadcasting) "camera" else "monitor"
                } else {
                    // Default to the role of the most recent activity if sitting on dashboard
                    cameraActivities.firstOrNull()?.role ?: "monitor"
                }
            }
            val filteredActivities = cameraActivities.filter { it.role == targetRole }

            if (filteredActivities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activity recorded for this mode",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                filteredActivities.take(5).forEach { activity ->
                    ActivityItem(
                        title = activity.title,
                        subtitle = activity.subtitle,
                        time = formatActivityTime(activity.timestamp),
                        iconType = activity.iconType
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 7. Upgrade Card
        UpgradeToMaxCard()

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showPermissionDialog) {
        CustomPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onOpenSettings = {
                showPermissionDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
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
                showAddCameraDialog = false
                viewModel.fetchAndSaveDeviceMetadata(sessionId)
                viewModel.startViewing(sessionId)
            }
        )
    }

    if (showManualPairingDialog) {
        ManualPairingDialog(
            onDismiss = { showManualPairingDialog = false },
            onConnect = { sessionId: String ->
                showManualPairingDialog = false
                viewModel.fetchAndSaveDeviceMetadata(sessionId)
                viewModel.startViewing(sessionId)
            }
        )
    }

    if (showIPCameraSetupDialog) {
        IPCameraSetupDialog(
            onDismiss = { showIPCameraSetupDialog = false },
            onNavigateToPlayerScreen = { ip, port, user, pass, path, name ->
                showIPCameraSetupDialog = false
                val encodedPath = path.replace("/", "|")
                navController.navigate(
                    "player/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${
                        Uri.encode(
                            pass
                        )
                    }/${Uri.encode(encodedPath)}/${Uri.encode(name)}"
                )
            },
            onNavigateToMultiChannel = { ip, port, user, pass, channels, main, brand ->
                showIPCameraSetupDialog = false
                navController.navigate(
                    "multi_channel/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${
                        Uri.encode(
                            pass
                        )
                    }/$channels/$main/$brand"
                )
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
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
    onStartCamera: () -> Unit,
    onCardClick: () -> Unit
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
        modifier = Modifier
            .size(width = cardWidth, height = cardHeight)
            .clickable(enabled = isBroadcasting) { onCardClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        if (isBroadcasting) {
            Box(modifier = Modifier.fillMaxSize()) {
                var localSink by remember { mutableStateOf<VideoSink?>(null) }

                DisposableEffect(sessionId) {
                    onDispose {
                        localSink?.let { viewModel.removeLocalSink(it) }
                    }
                }

                LaunchedEffect(localSink, sessionId) {
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
                            viewModel.initRenderer(this, isLocal = true)
                            localSink = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlays
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)) {
                    Row(
                        modifier = Modifier.align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Live Indicator (Red if connected, Gray if just sharing)
                        Row(
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(
                                    color = if (isConnected) Color.Red else Color.Gray,
                                    radius = size.minDimension / 2
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isConnected) "LIVE" else "WAITING",
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
                        onClick = { viewModel.stopStreaming() },
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

                    // 6. Waiting Overlay (if not connected, show a subtle prompt)
                    if (!isConnected) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Waiting for viewers...",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
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
                        Icons.Default.Videocam,
                        contentDescription = "Start",
                        tint = Color(0xFF77AEFF),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Connect Camera",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Broadcast this device to a monitor",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onStartCamera() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2939))
                ) {
                    Text(
                        "Start Camera",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
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

            DisposableEffect(sessionId) {
                onDispose {
                    remoteSink?.let { viewModel.removeRemoteSink(it) }
                }
            }

            LaunchedEffect(remoteSink, sessionId) {
                remoteSink?.let { sink ->
                    viewModel.startRemotePreview(sessionId, sink)
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
                        viewModel.initRenderer(this, isLocal = false)
                        remoteSink = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlays
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)) {
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
        modifier = Modifier
            .size(width = 240.dp, height = 300.dp)
            .clickable { onClick() },
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
fun DeviceRowItem(name: String, timestamp: Long, status: String, onActivate: () -> Unit) {
    val joinedText = remember(timestamp) {
        val diff = System.currentTimeMillis() - timestamp
        val days = diff / (1000 * 60 * 60 * 24)
        if (days <= 0) "JOINED TODAY" else "JOINED ${days}D AGO"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1C222B),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.smartphone),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = joinedText,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Status Capsule
            Surface(
                color = Color(0xFF1B232D).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = status,
                    color = if (status == "ONLINE") Color(0xFF4CAF50) else Color(0xFF9CA3AF).copy(
                        alpha = 0.6f
                    ),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Activate",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, time: String, iconType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F25))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getActivityIconColor(iconType).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getActivityIcon(iconType),
                    contentDescription = null,
                    tint = getActivityIconColor(iconType),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
            }

            Text(
                text = time,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
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
                        Text("SEE MAX", color = Color.Black, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/*@Composable
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
                                            viewModel.viewerDeviceId.value = connectedDeviceId
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
}*/

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
                                                    cameraViewModel?.viewerDeviceId?.value =
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
                            cameraViewModel?.viewerDeviceId?.value = deviceId
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("ALL MEDIA") }
    var selectedCamera by remember { mutableStateOf("ALL CAMERAS") }
    var selectedTime by remember { mutableStateOf("ANY TIME") }
    var showFilters by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showMediaTypeMenu by remember { mutableStateOf(false) }
    var showCameraMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val textGrey = Color(0xFF9CA3AF)
    val cardBackground = Color(0xFF161B22)

    val videoRecords by viewModel.videoRecords.observeAsState(emptyList())
    val snapshots by viewModel.snapshots.observeAsState(emptyList())
    val isLoadingVideos by viewModel.isLoadingVideos.observeAsState(false)
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())

    val userPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val profileImageUri = userPrefs.getString("profile_image", "")

    val filteredClips = remember(videoRecords, searchQuery, selectedCamera) {
        videoRecords.filter { record ->
            val matchesSearch = record.name.contains(searchQuery, ignoreCase = true)
            val matchesCamera = when (selectedCamera) {
                "ALL CAMERAS" -> true
                "RECORD" -> record.name.startsWith("REC_") && record.file.extension != "m4a" && !record.name.startsWith(
                    "MON_"
                )

                "MONITOR" -> record.file.extension == "m4a" || record.name.startsWith("MON_")
                "IP CAMERA" -> record.name.startsWith("RTSP_")
                else -> record.name.contains(selectedCamera, ignoreCase = true)
            }
            matchesSearch && matchesCamera
        }
    }

    val filteredSnapshots = remember(snapshots, searchQuery, selectedCamera) {
        snapshots.filter { record ->
            val matchesSearch = record.name.contains(searchQuery, ignoreCase = true)
            val matchesCamera = when (selectedCamera) {
                "ALL CAMERAS" -> true
                "RECORD" -> record.name.startsWith("REC_") && record.file.extension != "m4a" && !record.name.startsWith(
                    "MON_"
                )

                "MONITOR" -> record.file.extension == "m4a" || record.name.startsWith("MON_")
                "IP CAMERA" -> record.name.startsWith("RTSP_")
                else -> record.name.contains(selectedCamera, ignoreCase = true)
            }
            matchesSearch && matchesCamera
        }
    }

    val todayDateStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val dateSdf = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }

    val todayClipsCount = remember(filteredClips) {
        filteredClips.count { dateSdf.format(Date(it.file.lastModified())) == todayDateStr }
    }
    val todaySnapshotsCount = remember(filteredSnapshots) {
        filteredSnapshots.count { dateSdf.format(Date(it.file.lastModified())) == todayDateStr }
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
        if (isGranted) viewModel.refreshVideoRecords()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                storagePermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.refreshVideoRecords()
        } else {
            storagePermissionLauncher.launch(storagePermission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C222B)
            ) {
                if (!profileImageUri.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "NannyEye",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${videoRecords.size} CLIPS • ${snapshots.size} SNAPSHOTS",
                    color = textGrey,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search recordings...", color = textGrey, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    null,
                    tint = textGrey,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Tune,
                    null,
                    tint = textGrey,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showFilters = !showFilters }
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = cardBackground,
                unfocusedContainerColor = cardBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Filters Row
        if (showFilters) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipDropdown(
                        text = selectedMediaType,
                        onClick = { showMediaTypeMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showMediaTypeMenu,
                        onDismissRequest = { showMediaTypeMenu = false },
                        modifier = Modifier.background(cardBackground)
                    ) {
                        listOf("ALL MEDIA", "CLIPS", "SNAPSHOTS").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedMediaType = type
                                    showMediaTypeMenu = false
                                    showFilters = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    FilterChipDropdown(
                        text = selectedCamera,
                        onClick = { showCameraMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showCameraMenu,
                        onDismissRequest = { showCameraMenu = false },
                        modifier = Modifier.background(cardBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("ALL CAMERAS", color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                selectedCamera = "ALL CAMERAS"
                                showCameraMenu = false
                                showFilters = false
                            }
                        )
                        listOf("RECORD", "MONITOR", "IP CAMERA").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedCamera = option
                                    showCameraMenu = false
                                    showFilters = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    FilterChipDropdown(
                        text = selectedTime,
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            selectedTime =
                                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                                    .uppercase()
                            viewModel.refreshVideoRecords(date)
                            showFilters = false
                        }
                        showDatePicker = false
                    }) { Text("OK", color = Color(0xFF77AEFF)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedTime = "ANY TIME"
                        viewModel.refreshVideoRecords(null)
                        showFilters = false
                        showDatePicker = false
                    }) { Text("Clear", color = Color.Red) }
                }
            ) { DatePicker(state = datePickerState) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoadingVideos) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF77AEFF))
            }
        } else {
            // 4. Clips Section
            if (selectedMediaType == "ALL MEDIA" || selectedMediaType == "CLIPS") {
                SectionHeader(title = "Clips", countText = "$todayClipsCount CAPTURED TODAY")
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredClips.isEmpty()) {
                    EmptySectionPlaceholder("No clips found")
                } else {
                    val previewClips = filteredClips.take(5)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(previewClips) { clip ->
                            ClipThumbnail(clip) {
                                playMedia(context, clip.file, "video/*")
                            }
                        }
                        if (filteredClips.size > 5) {
                            item {
                                ViewAllCard(Modifier.size(140.dp)) {
                                    navController.navigate("all_devices_records")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Snapshots Section
            if (selectedMediaType == "ALL MEDIA" || selectedMediaType == "SNAPSHOTS") {
                SectionHeader(
                    title = "Snapshots",
                    countText = "$todaySnapshotsCount CAPTURED TODAY"
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredSnapshots.isEmpty()) {
                    EmptySectionPlaceholder("No snapshots found")
                } else {
                    val previewSnapshots = filteredSnapshots.take(5)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(previewSnapshots) { snapshot ->
                            SnapshotThumbnail(
                                record = snapshot,
                                modifier = Modifier.size(110.dp)
                            ) {
                                playMedia(context, snapshot.file, "image/*")
                            }
                        }
                        if (filteredSnapshots.size > 5) {
                            item {
                                ViewAllCard(Modifier.size(110.dp)) {
                                    navController.navigate("all_devices_records")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun FilterChipDropdown(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        color = Color(0xFF161B22),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, countText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = countText.uppercase(),
            color = Color(0xFF9CA3AF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ClipThumbnail(record: CameraViewModel.VideoRecord, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .clickable { onClick() }
    ) {
        if (record.thumbnail != null) {
            Image(
                bitmap = record.thumbnail.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)))
        }

        // Duration Tag
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = record.duration,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Play Icon
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun SnapshotThumbnail(
    record: CameraViewModel.VideoRecord,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C222B))
            .clickable { onClick() }
    ) {
        if (record.thumbnail != null) {
            Image(
                bitmap = record.thumbnail.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ViewAllCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(0xFF1B232D)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF77AEFF),
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "VIEW ALL",
                color = Color(0xFF77AEFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}

internal fun playMedia(context: Context, file: File, mimeType: String) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Cannot open file",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}


@Composable
fun DevicesContent(
    navController: NavController,
    viewModel: CameraViewModel,
    onNavigateToDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val textGrey = Color(0xFF9CA3AF)
    val cardBackground = Color(0xFF161B22)

    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())
    val savedViewers by viewModel.savedViewers.observeAsState(emptyList())
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val roomDevices by viewModel.roomDevices.observeAsState(emptyList())
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())
    val isBroadcasting by viewModel.isBroadcasting.observeAsState(false)
    val sessionId by viewModel.sessionId.observeAsState("")
    val myDeviceId by viewModel.myDeviceId.observeAsState("")
    val myDeviceName by viewModel.deviceName.observeAsState("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")

    val userPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val profileImageUri = userPrefs.getString("profile_image", "")
    val fullName = userPrefs.getString("full_name", "User") ?: "User"

    val monitorDevices = savedDevices.filter { it.role == "monitor" }
    val activeMonitorsCount = monitorDevices.count { device ->
        onlineMonitors[device.deviceId]?.sessionId == device.sessionId
    }

    var showIPCameraSetup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1116))
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1C222B)
            ) {
                if (!profileImageUri.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "NannyEye",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(color = Color(0xFFFF8A80))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE • $activeMonitorsCount MONITORS ACTIVE",
                        color = textGrey,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 2. This device Section
        SectionHeaderWithAction(
            title = "This device",
            actionText = "CURRENT DEVICE",
            onActionClick = {}
        )
        Spacer(modifier = Modifier.height(12.dp))
        DeviceCardItem(
            icon = ImageVector.vectorResource(id = R.drawable.smartphone),
            title = myDeviceName,
            subtitle = if (isBroadcasting) "SESSION: $sessionId • ACTIVE" else "ID: $myDeviceId • STANDBY",
            badge = if (isBroadcasting) "ONLINE" else "OFFLINE",
            badgeColor = if (isBroadcasting) Color(0xFF4CAF50) else textGrey,
            onClick = {
                if (isBroadcasting && sessionId.isNotEmpty()) {
                    navController.navigate("camera_view/${Uri.encode(sessionId)}")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Monitors Section (Showing Connected Hosts/Monitors)
        SectionHeaderWithAction(
            title = "Cameras",
            subtitle = "${monitorDevices.size} CONNECTED DEVICES",
            actionContent = {
                Button(
                    onClick = { navController.navigate("add_device") },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFBBC6E2).copy(alpha = 0.8f), Color(0xFF1B263B).copy(alpha = 0.8f))
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ ADD CAMERA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (monitorDevices.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Videocam,
                text = "No monitors connected",
                subtext = "ADD A MONITOR TO START MONITORING"
            )
        } else {
            monitorDevices.forEach { device ->
                val onlineInfo = onlineMonitors[device.deviceId]
                val isLive = onlineInfo != null && onlineInfo.sessionId.isNotEmpty()
                val currentSessionId = onlineInfo?.sessionId ?: device.sessionId

                CameraRowItem(
                    name = device.name,
                    status = if (isLive) "LIVE • $currentSessionId" else "${device.connectionStatus} • LAST SEEN ${formatTimestamp(Timestamp(Date(device.lastSeen)))}",
                    isLive = isLive,
                    onActivate = {
                        if (isLive) {
                            navController.navigate("viewer/${Uri.encode(currentSessionId)}")
                        }
                    },
                    onDelete = { viewModel.deleteSavedDevice(device.deviceId) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Cameras Section (Showing Connected Viewers)
        SectionHeaderWithAction(
            title = "Monitors",
            subtitle = "${savedViewers.size} CONNECTED MONITORS",
            actionText = "SHARE THIS CAMERA",
            actionIcon = Icons.Default.Share,
            onActionClick = { /* Share QR */ }
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (savedViewers.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Laptop,
                text = "No active viewers",
                subtext = "WATCHING THIS DEVICE"
            )
        } else {
            savedViewers.forEach { viewer ->
                val isCurrentlyActive = connectedViewers.any { it.deviceId == viewer.deviceId }
                MonitorRowItem(
                    name = viewer.name,
                    status = if (isCurrentlyActive) "SHARING" else viewer.connectionStatus,
                    subtext = if (isCurrentlyActive) "WATCHING THIS DEVICE" else "LAST SEEN ${formatTimestamp(Timestamp(Date(viewer.lastSeen)))}",
                    onDisconnect = {
                        // 1. Stop current sharing/viewing
                        val sid = viewer.sessionId
                        viewModel.resumeHostSession(
                            sid = sid,
                            rId = viewer.roomId,
                            hId = viewer.hostId,
                            targetViewerId = viewer.deviceId
                        )
                        // Navigate back to Dashboard
                        onNavigateToDashboard()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. IP Camera Section
        SectionHeaderWithAction(
            title = "IP Camera",
            subtitle = "NETWORK DEVICES",
            actionText = "Add IP CAMERA",
            actionIcon = Icons.Default.Add,
            onActionClick = { showIPCameraSetup = true }
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (roomDevices.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Router,
                text = "No IP cameras detected",
                subtext = "CHECK YOUR LOCAL NETWORK CONNECTION"
            )
        } else {
            roomDevices.forEach { device ->
                IPCameraRowItem(
                    device = device,
                    navController = navController,
                    onDelete = { viewModel.deleteRoomDevice(device) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Footer
        Text(
            text = "NANNYEYE V2.4.8 — STABLE",
            color = textGrey.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showIPCameraSetup) {
        IPCameraSetupDialog(
            onDismiss = { showIPCameraSetup = false },
            onNavigateToPlayerScreen = { ip, port, user, pass, path, name ->
                showIPCameraSetup = false
                val encodedPath = path.replace("/", "|")
                navController.navigate(
                    "player/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${
                        Uri.encode(
                            pass
                        )
                    }/${Uri.encode(encodedPath)}/${Uri.encode(name)}"
                )
            },
            onNavigateToMultiChannel = { ip, port, user, pass, channels, main, brand ->
                showIPCameraSetup = false
                navController.navigate(
                    "multi_channel/${Uri.encode(ip)}/$port/${Uri.encode(user)}/${
                        Uri.encode(
                            pass
                        )
                    }/$channels/$main/$brand"
                )
            },
            cameraViewModel = viewModel
        )
    }
}

@Composable
fun SectionHeaderWithAction(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
    actionContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        if (actionContent != null) {
            actionContent()
        } else if (actionText != null) {
            Row(
                modifier = Modifier.clickable { onActionClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionText.uppercase(),
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (actionIcon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCardItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1C222B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CameraRowItem(
    name: String,
    status: String,
    isLive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1C222B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status.uppercase(),
                    color = if (isLive) Color(0xFF4CAF50) else Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isLive) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Activate", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF9CA3AF).copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MonitorRowItem(
    name: String,
    status: String,
    subtext: String,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1C222B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Laptop,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $status",
                        color = Color(0xFF9CA3AF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = subtext,
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            /*if (onReconnect != null && status != "SHARING") {
                Button(
                    onClick = onReconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reconnect", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }*/

            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Disconnect",
                    tint = Color(0xFF9CA3AF).copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun IPCameraRowItem(
    device: Device,
    navController: NavController,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1C222B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${device.brand} • ${device.ip}",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    val channelCount = device.channelCount
                    if (channelCount <= 1) {
                        val encodedPath = device.path.replace("/", "|")
                        navController.navigate("player/${Uri.encode(device.ip)}/${device.port}/${Uri.encode(device.username)}/${Uri.encode(device.password)}/${Uri.encode(encodedPath)}/${Uri.encode(device.name)}")
                    } else {
                        navController.navigate("multi_channel/${Uri.encode(device.ip)}/${device.port}/${Uri.encode(device.username)}/${Uri.encode(device.password)}/$channelCount/${device.mainStream}/${device.brand}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B232D)),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF9CA3AF).copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(icon: ImageVector, text: String, subtext: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1C222B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtext,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettingsOptionItem(
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

fun formatActivityTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun getActivityIcon(type: String): ImageVector {
    return when (type) {
        "camera_start" -> Icons.Default.Videocam
        "camera_stop" -> Icons.Default.VideocamOff
        "monitor_connect" -> Icons.Default.Podcasts
        "monitor_disconnect" -> Icons.Default.Close
        "reconnecting" -> Icons.Default.Refresh
        "connection_lost" -> Icons.Outlined.ErrorOutline
        "live_start" -> Icons.Default.PlayCircle
        "live_end" -> Icons.Default.Adjust
        "audio_mute" -> Icons.Default.MicOff
        "audio_unmute" -> Icons.Default.Mic
        "viewer_connect" -> Icons.Default.Person
        "viewer_disconnect" -> Icons.Default.Person
        "screenshot" -> Icons.Default.Visibility
        "video_record" -> Icons.Default.History
        else -> Icons.Default.CheckCircle
    }
}

fun getActivityIconColor(type: String): Color {
    return when (type) {
        "camera_start", "monitor_connect", "live_start", "audio_unmute", "viewer_connect" -> Color(
            0xFF77AEFF
        )

        "camera_stop", "monitor_disconnect", "connection_lost", "live_end", "audio_mute", "viewer_disconnect" -> Color(
            0xFFFF8A80
        )

        "reconnecting" -> Color(0xFFFFD54F)
        "screenshot", "video_record" -> Color(0xFFBBC6E2)
        else -> Color.White
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllActivitiesScreen(
    navController: NavController,
    viewModel: CameraViewModel
) {
    val cameraActivities by viewModel.cameraActivities.observeAsState(emptyList())
    val sessionId by viewModel.sessionId.observeAsState("")
    val isBroadcasting by viewModel.isBroadcasting.observeAsState(false)

    val targetRole = remember(sessionId, isBroadcasting, cameraActivities) {
        if (sessionId.isNotEmpty()) {
            if (isBroadcasting) "camera" else "monitor"
        } else {
            cameraActivities.firstOrNull()?.role ?: "monitor"
        }
    }
    val filteredActivities = cameraActivities.filter { it.role == targetRole }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Camera Activity",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E1116)
                )
            )
        },
        containerColor = Color(0xFF0E1116)
    ) { padding ->
        if (filteredActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No activity recorded for this mode", color = Color(0xFF9CA3AF))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredActivities) { activity ->
                    ActivityItem(
                        title = activity.title,
                        subtitle = activity.subtitle,
                        time = formatActivityTime(activity.timestamp),
                        iconType = activity.iconType
                    )
                }
            }
        }
    }
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
                            Icon(
                                Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Add the code manually",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
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
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
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

    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
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
                Text(
                    "Enter the 9 or 10-digit code shown on the camera device.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDevicesScreen(
    onBack: () -> Unit,
    onActivate: (String, String) -> Unit, // Added role parameter
    viewModel: CameraViewModel
) {
    val savedDevices by viewModel.savedDevices.observeAsState(emptyList())
    val savedViewers by viewModel.savedViewers.observeAsState(emptyList())
    val onlineMonitors by viewModel.onlineMonitors.observeAsState(emptyMap())
    val connectedViewers by viewModel.connectedViewers.observeAsState(emptyList())

    val allRemoteDevices = remember(savedDevices, savedViewers) {
        val list = mutableListOf<Triple<String, Long, String>>() // Name, Timestamp, Role (for UI)
        val data = mutableListOf<Map<String, Any>>()

        savedDevices.forEach {
            data.add(mapOf("id" to it.deviceId, "name" to it.name, "time" to it.timestamp, "role" to it.role, "sid" to it.sessionId))
        }
        savedViewers.forEach {
            data.add(mapOf("id" to it.deviceId, "name" to it.name, "time" to it.timestamp, "role" to "viewer", "sid" to it.sessionId))
        }
        
        data.sortedByDescending { it["time"] as Long }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recently Joined",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E1116)
                )
            )
        },
        containerColor = Color(0xFF0E1116)
    ) { padding ->
        if (allRemoteDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No remote devices joined yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allRemoteDevices) { deviceMap ->
                    val dId = deviceMap["id"] as String
                    val name = deviceMap["name"] as String
                    val timestamp = deviceMap["time"] as Long
                    val role = deviceMap["role"] as String
                    val sid = deviceMap["sid"] as String

                    val isLive = if (role == "viewer") {
                        connectedViewers.any { it.deviceId == dId }
                    } else {
                        onlineMonitors[dId]?.sessionId == sid
                    }

                    DeviceRowItem(
                        name = name,
                        timestamp = timestamp,
                        status = if (isLive) "ONLINE" else "OFFLINE",
                        onActivate = {
                            if (isLive) {
                                onActivate(sid, role)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    viewModel: CameraViewModel,
    onItemSelected: (Int) -> Unit
) {
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
        stringResource(id = R.string.devices),
        "PREMIUM",
        stringResource(id = R.string.records),
        stringResource(id = R.string.alerts)
    )
    val icons = listOf(
        Icons.Default.GridView,
        Icons.Default.Videocam,
        Icons.Default.Star,
        Icons.Default.Folder,
        Icons.Default.Notifications
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
                        Box {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                tint = contentColor,
                                modifier = Modifier.size(iconSize)
                            )
                            /*if (index == 4 && unreadCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp),
                                    shape = CircleShape,
                                    color = Color.Red,
                                    border = BorderStroke(1.dp, navBackground)
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }*/
                        }
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
