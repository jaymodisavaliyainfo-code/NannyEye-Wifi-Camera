package monitoringcamera.transmitterconnect.officeconnectcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.animation.core.keyframes
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    onBack: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToCameraView: (String) -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToMultiChannel: (String, Int, String, String, Int, Boolean, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToPlayerScreen: (String, Int, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    cameraViewModel: CameraViewModel = viewModel()
) {
    var selectedOption by remember { mutableIntStateOf(1) } // Default to "Use as a Monitor"
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var showIPCameraSetup by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isConnected by cameraViewModel.isConnected.observeAsState(false)
    val sessionId by cameraViewModel.sessionId.observeAsState("")

    val activity = context as Activity

    // Permissions
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            if (selectedOption == 0) {
                onNavigateToScanner()
            } else if (selectedOption == 1) {
                showQRCodeDialog = true
            } else if (selectedOption == 2) {
                showIPCameraSetup = true
            }
        } else {
            // Check if user denied permanently
            val cameraGranted = results[Manifest.permission.CAMERA] ?: false
            val audioGranted = results[Manifest.permission.RECORD_AUDIO] ?: false

            val cameraPermanentlyDenied = !cameraGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.CAMERA
            )
            val audioPermanentlyDenied = !audioGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.RECORD_AUDIO
            )

            if (cameraPermanentlyDenied || audioPermanentlyDenied) {
                showPermissionDialog = true
            }
        }
    }

    val darkBackground = Color(0xFF0E1116)
    val textGrey = Color(0xFF9CA3AF)
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
    )

    // When connected as a camera (after being scanned), navigate to CameraView
    LaunchedEffect(isConnected) {
        if (isConnected && showQRCodeDialog) {
            showQRCodeDialog = false
            onNavigateToCameraView(sessionId)
        }
    }

    if (showQRCodeDialog) {
        val qrBitmap by cameraViewModel.qrBitmap.observeAsState()

        // Generate new Session ID and QR code when dialog is shown
        LaunchedEffect(Unit) {
            cameraViewModel.generateHostSession()
            cameraViewModel.startStreaming(null, false)
        }

        QRCodeDialog(
            onDismiss = { showQRCodeDialog = false },
            qrBitmap = qrBitmap,
            sessionId = sessionId,
            onShowIPCamera = {
                showQRCodeDialog = false
                showIPCameraSetup = true
            },
            onNavigateToHelp = onNavigateToHelp,
            cameraViewModel = cameraViewModel
        )
    }

    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            color = Color.White,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() },
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(id = R.dimen.screen_padding))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

            Text(
                text = stringResource(id = R.string.add_new_device),
                color = Color.White,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() },
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

            Text(
                text = stringResource(id = R.string.add_device_description),
                color = textGrey,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                lineHeight = with(density) { dimensionResource(id = R.dimen.text_title).toSp() }
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

            DeviceOptionItem(
                title = stringResource(id = R.string.use_as_camera),
                description = stringResource(id = R.string.use_as_camera_desc),
                icon = ImageVector.vectorResource(id = R.drawable.locationon),
                isSelected = selectedOption == 1,
                onClick = { selectedOption = 1 }
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

            DeviceOptionItem(
                title = stringResource(id = R.string.use_as_monitor),
                description = stringResource(id = R.string.use_as_monitor_desc),
                icon = ImageVector.vectorResource(id = R.drawable.videocam),
                isSelected = selectedOption == 0,
                onClick = { selectedOption = 0 }
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

            DeviceOptionItem(
                title = stringResource(id = R.string.add_ip_camera),
                description = stringResource(id = R.string.add_ip_camera_desc),
                icon = ImageVector.vectorResource(id = R.drawable.home),
                isSelected = selectedOption == 2,
                onClick = { selectedOption = 2 }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (AdsDataHolder.adsData != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth(), factory = {
                        FrameLayout(it).apply {
                            FacebookAds.getInstance(activity).ShowSmallNativeBannerAd(
                                activity, AdsDataHolder.adsData.qurekaNativeBannerUrl2, this
                            )
                        }
                    })
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_height_large))
                    .shadow(
                        elevation = dimensionResource(id = R.dimen.element_spacing),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                        ambientColor = Color(0xFF77AEFF).copy(alpha = 0.5f),
                        spotColor = Color(0xFF77AEFF).copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                    .background(primaryGradient)
                    .clickable {
                        val allGranted = permissions.all {
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        }
                        if (allGranted) {
                            if (selectedOption == 0) {
                                onNavigateToScanner()
                            } else if (selectedOption == 1) {
                                showQRCodeDialog = true
                            } else if (selectedOption == 2) {
                                showIPCameraSetup = true
                            }
                        } else {
                            permissionLauncher.launch(permissions)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(id = R.string.next),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() }
                    )
                }
            }

            if (showIPCameraSetup) {
                IPCameraSetupDialog(
                    onDismiss = { showIPCameraSetup = false },
                    onNavigateToMultiChannel = onNavigateToMultiChannel,
                    onNavigateToPlayerScreen = onNavigateToPlayerScreen,
                    cameraViewModel = cameraViewModel
                )
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

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))
        }
    }
}

@Composable
fun DeviceOptionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardBackground = Color(0xFF181C22)
    val textGrey = Color(0xFF9CA3AF)

    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) dimensionResource(id = R.dimen.spacer_nano) else dimensionResource(id = R.dimen.spacer_none),
                color = if (isSelected) colorResource(id = R.color.selected_stroke) else Color.Transparent,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
            ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.card_padding))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.option_icon_container))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_small)))
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1B263B), Color(0xFFBBC6E2))
                                )
                            )
                        } else {
                            Modifier.background(Color(0xFF31353C))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFFBBC6E2),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() },
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                    lineHeight = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFBBC6E2),
                    unselectedColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun QRCodeDialog(
    onDismiss: () -> Unit,
    qrBitmap: Bitmap?,
    sessionId: String,
    onShowIPCamera: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    cameraViewModel: CameraViewModel = viewModel()
) {
    val isConnected by cameraViewModel.isConnected.observeAsState(false)

    DisposableEffect(Unit) {
        onDispose {
            if (!isConnected) {
                cameraViewModel.stopAll()
            }
        }
    }

    val darkBackground = Color(0xFF14171C)
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
    )

    val density = LocalDensity.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = dimensionResource(id = R.dimen.padding_medium),
                    bottom = dimensionResource(id = R.dimen.padding_medium)), // To show some of the background like in the image
            color = darkBackground,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.spacer_small))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.screen_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                Text(
                    text = stringResource(id = R.string.scan_qr_instruction),
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_h2).toSp() },
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() }
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                Text(
                    text = stringResource(id = R.string.scan_qr_description),
                    color = textGrey,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                    textAlign = TextAlign.Center,
                    lineHeight = with(density) { dimensionResource(id = R.dimen.text_body).toSp() },
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.screen_padding))
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                // QR Code Container with Focus Corners
                Box(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.qr_container_size)),
                    contentAlignment = Alignment.Center
                ) {
                    // Scanning Line Animation
                    val infiniteTransition = rememberInfiniteTransition(label = "lineAnimation")
                    val lineY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "lineY"
                    )

                    // Custom Focus Corners
                    val strokeWidth = dimensionResource(id = R.dimen.spacer_nano)
                    val cornerSize = dimensionResource(id = R.dimen.icon_size_medium)
                    val radius = dimensionResource(id = R.dimen.element_spacing)
                    val padding = dimensionResource(id = R.dimen.spacer_small)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = strokeWidth.toPx()
                        val cornerSizePx = cornerSize.toPx()
                        val radiusPx = radius.toPx()
                        val paddingPx = padding.toPx()

                        // Top Left
                        val pathTL = androidx.compose.ui.graphics.Path().apply {
                            moveTo(paddingPx, cornerSizePx + paddingPx)
                            lineTo(paddingPx, radiusPx + paddingPx)
                            quadraticTo(paddingPx, paddingPx, radiusPx + paddingPx, paddingPx)
                            lineTo(cornerSizePx + paddingPx, paddingPx)
                        }
                        drawPath(
                            pathTL,
                            Color.White,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidthPx)
                        )

                        // Top Right
                        val pathTR = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width - cornerSizePx - paddingPx, paddingPx)
                            lineTo(size.width - radiusPx - paddingPx, paddingPx)
                            quadraticTo(
                                size.width - paddingPx,
                                paddingPx,
                                size.width - paddingPx,
                                radiusPx + paddingPx
                            )
                            lineTo(size.width - paddingPx, cornerSizePx + paddingPx)
                        }
                        drawPath(
                            pathTR,
                            Color.White,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidthPx)
                        )

                        // Bottom Left
                        val pathBL = androidx.compose.ui.graphics.Path().apply {
                            moveTo(paddingPx, size.height - cornerSizePx - paddingPx)
                            lineTo(paddingPx, size.height - radiusPx - paddingPx)
                            quadraticTo(
                                paddingPx,
                                size.height - paddingPx,
                                radiusPx + paddingPx,
                                size.height - paddingPx
                            )
                            lineTo(cornerSizePx + paddingPx, size.height - paddingPx)
                        }
                        drawPath(
                            pathBL,
                            Color.White,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidthPx)
                        )

                        // Bottom Right
                        val pathBR = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width - cornerSizePx - paddingPx, size.height - paddingPx)
                            lineTo(size.width - radiusPx - paddingPx, size.height - paddingPx)
                            quadraticTo(
                                size.width - paddingPx,
                                size.height - paddingPx,
                                size.width - paddingPx,
                                size.height - radiusPx - paddingPx
                            )
                            lineTo(size.width - paddingPx, size.height - cornerSizePx - paddingPx)
                        }
                        drawPath(
                            pathBR,
                            Color.White,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidthPx)
                        )
                    }

                    // Scanning Line
                    val qrCodeSize = dimensionResource(id = R.dimen.qr_code_size)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(dimensionResource(id = R.dimen.spacer_nano))
                            .offset(y = (- (qrCodeSize / 2) + (qrCodeSize * lineY)))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFFBBC6E2),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Surface(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.qr_code_size)),
                        color = Color.White,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_large))
                    ) {
                        Surface(
                            modifier = Modifier.size(dimensionResource(id = R.dimen.qr_inner_size)),
                            color = Color.White,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.padding(dimensionResource(id = R.dimen.spacer_micro))
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.screen_padding)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    Text(
                        text = stringResource(id = R.string.or_separator),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.card_padding))
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                // Send the link Button
                Button(
                    onClick = { /* Handle send link */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height_large))
                        .padding(horizontal = dimensionResource(id = R.dimen.spacer_small)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(id = R.string.send_link),
                                color = Color(0xFF1B263B),
                                fontWeight = FontWeight.Medium,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF1B263B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                // Add ip camera Button
                Button(
                    onClick = {
                        onShowIPCamera()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.button_height_large))
                        .padding(horizontal = dimensionResource(id = R.dimen.spacer_small)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262B33))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                        Text(
                            stringResource(id = R.string.add_ip_camera),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                // Need help text

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))

                Row(
                    modifier = Modifier
                        .clickable { onNavigateToHelp() }
                        .padding(dimensionResource(id = R.dimen.spacer_small)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = textGrey,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                    Text(
                        text = stringResource(id = R.string.need_help_text),
                        color = textGrey,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.screen_padding)))
            }
        }
    }

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPCameraSetupDialog(
    deviceToEdit: Device? = null,
    onDismiss: () -> Unit,
    onNavigateToMultiChannel: (String, Int, String, String, Int, Boolean, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToPlayerScreen: (String, Int, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(deviceToEdit?.name ?: "") }
    var ip by remember { mutableStateOf(deviceToEdit?.ip ?: "") }
    var port by remember { mutableStateOf(deviceToEdit?.port?.toString() ?: "554") }
    var username by remember { mutableStateOf(deviceToEdit?.username ?: "admin") }
    var password by remember { mutableStateOf(deviceToEdit?.password ?: "") }
    var selectedBrand by remember {
        mutableStateOf(
            if (deviceToEdit != null) CameraUrlBuilder.Brand.valueOf(deviceToEdit.brand)
            else CameraUrlBuilder.Brand.DAHUA
        )
    }
    var isMainStream by remember { mutableStateOf(deviceToEdit?.mainStream ?: true) }
    var channelCount by remember { mutableStateOf(deviceToEdit?.channelCount?.toString() ?: "1") }
    var useCustomPort by remember { mutableStateOf(deviceToEdit?.port != 554 && deviceToEdit != null) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showScanner by remember { mutableStateOf(false) }
    val networkScanner = remember { NetworkScanner() }
    val foundDevices = remember { mutableStateListOf<NetworkScanner.FoundDevice>() }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var isScanComplete by remember { mutableStateOf(false) }

    val bgColor = Color(0xFF0E1116) // Matches Project Dark Background
    val cardBg = Color(0xFF1B1F26) // Matches Project Card Background
    val textGrey = Color(0xFF9CA3AF) // Matches Project Text Grey
    val accentColor = Color(0xFFBBC6E2) // Matches Project Accent
    val brandList = CameraUrlBuilder.Brand.entries.toTypedArray()

    val fillRequiredFields = stringResource(id = R.string.fill_required_fields)
    val cameraUpdated = stringResource(id = R.string.camera_updated)
    val cameraAdded = stringResource(id = R.string.camera_added_successfully)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bgColor
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.add_ip_camera_title),
                            color = Color.White,
                            fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_subtitle).toSp() }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showScanner) showScanner = false else onDismiss()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back_content_desc),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = bgColor,
                        titleContentColor = Color.White
                    )
                )

                if (showScanner) {
                    // Network Scanner UI
                    val subnet = remember { networkScanner.getSubnet(networkScanner.getDeviceIp(context)) }

                    val density = LocalDensity.current
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(id = R.dimen.screen_padding)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                        // Box from Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen.image_box_height))
                                .border(
                                    dimensionResource(id = R.dimen.spacer_tiny),
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(dimensionResource(id = R.dimen.radius_large))
                                )
                                .padding(dimensionResource(id = R.dimen.card_padding)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Shield Icon with Progress
                                Box(
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.icon_size_large))
                                        .border(
                                            dimensionResource(id = R.dimen.spacer_nano),
                                            Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Progress Bar at top of shield box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopCenter)
                                            .padding(horizontal = dimensionResource(id = R.dimen.spacer_small), vertical = dimensionResource(id = R.dimen.spacer_micro))
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { scanProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(dimensionResource(id = R.dimen.spacer_micro))
                                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.spacer_nano))),
                                            color = accentColor,
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = accentColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
                                    )
                                }

                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                                Text(
                                    text = if (isScanComplete) "Completed" else stringResource(id = R.string.searching_network),
                                    color = Color.White,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() },
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                                Text(
                                    text = stringResource(id = R.string.search_description),
                                    color = textGrey,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() },
                                    textAlign = TextAlign.Center,
                                    lineHeight = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                        // Scanning Status Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen.status_bar_height)),
                            color = cardBg,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Animated Dot
                                    val infiniteTransition = rememberInfiniteTransition(label = "blink")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = keyframes {
                                                durationMillis = 1000
                                                0.7f at 500
                                            },
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "alpha"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(dimensionResource(id = R.dimen.spacer_small))
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(if (isScanComplete) Color(0xFF4CAF50) else Color(0xFFE57373).copy(alpha = if (isScanComplete) 1f else alpha))
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                                    Text(
                                        text = if (isScanComplete) "COMPLETED" else stringResource(id = R.string.scanning),
                                        color = Color.White,
                                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = with(LocalDensity.current) { dimensionResource(id = R.dimen.spacer_tiny).toSp() },
                                        modifier = if (isScanComplete) Modifier else Modifier.graphicsLayer(alpha = alpha)
                                    )
                                }

                                Text(
                                    text = stringResource(id = R.string.ip_range_label, subnet),
                                    color = textGrey,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                        // Found Devices List
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(foundDevices) { device ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimensionResource(id = R.dimen.spacer_micro))
                                        .clickable {
                                            ip = device.ip
                                            if (device.port != 554) {
                                                useCustomPort = true
                                                port = device.port.toString()
                                            }
                                            showScanner = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.spacer_nano))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.NetworkCheck,
                                            contentDescription = null,
                                            tint = accentColor
                                        )
                                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.card_padding)))
                                        Column {
                                            Text(
                                                device.ip,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Port ${device.port} \u00B7 ${device.responseMs}ms",
                                                color = textGrey,
                                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() }
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            stringResource(id = R.string.connect_caps),
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_padding)))

                        // Bottom Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = dimensionResource(id = R.dimen.card_padding)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.card_padding))
                        ) {
                            Button(
                                onClick = {
                                    networkScanner.cancel()
                                    showScanner = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensionResource(id = R.dimen.button_height)),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                            ) {
                                Text(
                                    stringResource(id = R.string.manual_ip),
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_body).toSp() }
                                )
                            }

                            Button(
                                onClick = {
                                    foundDevices.clear()
                                    isScanComplete = false
                                    networkScanner.cancel()
                                    // Trigger a re-scan by toggling or just calling it
                                            // In this setup, we can just call scan again
                                            foundDevices.clear()
                                            networkScanner.scan(context, object : NetworkScanner.ScanCallback {
                                                override fun onProgress(scanned: Int, total: Int, ip: String) {
                                                    scanProgress = scanned.toFloat() / total.toFloat()
                                                }

                                                override fun onDeviceFound(device: NetworkScanner.FoundDevice) {
                                                    foundDevices.add(device)
                                                }

                                                override fun onScanComplete(devices: List<NetworkScanner.FoundDevice>) {
                                                    isScanComplete = true
                                                }
                                            })
                                        },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensionResource(id = R.dimen.button_height)),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                            ) {
                                Text(
                                    stringResource(id = R.string.refresh),
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.text_body).toSp() }
                                )
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        foundDevices.clear()
                        isScanComplete = false
                        networkScanner.scan(context, object : NetworkScanner.ScanCallback {
                            override fun onProgress(scanned: Int, total: Int, ip: String) {
                                scanProgress = scanned.toFloat() / total.toFloat()
                            }

                            override fun onDeviceFound(device: NetworkScanner.FoundDevice) {
                                foundDevices.add(device)
                            }

                            override fun onScanComplete(devices: List<NetworkScanner.FoundDevice>) {
                                isScanComplete = true
                            }
                        })
                    }

                    DisposableEffect(Unit) {
                        onDispose { networkScanner.cancel() }
                    }
                } else {
                    // Camera Setup Form - Based on XML layout provided
                    val density = LocalDensity.current
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(id = R.dimen.screen_padding))
                    ) {
                        item {
                            // User Name Label & Input
                            Text(
                                stringResource(id = R.string.user_name),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacer_micro))
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.name_placeholder),
                                        color = textGrey.copy(alpha = 0.5f)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = textGrey,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                ),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                            // Camera Brand Label
                            Text(
                                stringResource(id = R.string.camera_brand),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacer_micro))
                            )
                            var expanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(id = R.dimen.button_height))
                                    .background(cardBg, RoundedCornerShape(dimensionResource(id = R.dimen.radius_small)))
                                    .border(
                                        dimensionResource(id = R.dimen.spacer_tiny),
                                        Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                                    )
                                    .clickable { expanded = true }
                                    .padding(horizontal = dimensionResource(id = R.dimen.element_spacing)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedBrand.displayName, color = Color.White)
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .background(cardBg)
                                ) {
                                    brandList.forEach { brand ->
                                        DropdownMenuItem(
                                            text = { Text(brand.displayName, color = Color.White) },
                                            onClick = {
                                                selectedBrand = brand
                                                username = CameraUrlBuilder.defaultUsername(brand)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_medium)))

                            // IP Address with Scan Icon
                            OutlinedTextField(
                                value = ip,
                                onValueChange = { ip = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        stringResource(id = R.string.ip_address),
                                        color = textGrey
                                    )
                                },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.ip_placeholder),
                                        color = textGrey.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showScanner = true }) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = stringResource(id = R.string.scan_content_desc),
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = textGrey,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                ),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                            // Custom Port Checkbox
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = useCustomPort,
                                    onCheckedChange = { useCustomPort = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = accentColor,
                                        uncheckedColor = textGrey
                                    )
                                )
                                Text(
                                    stringResource(id = R.string.use_custom_port),
                                    color = Color.White,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
                                )
                            }

                            if (useCustomPort) {
                                OutlinedTextField(
                                    value = port,
                                    onValueChange = { port = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = {
                                        Text(
                                            stringResource(id = R.string.port_label),
                                            color = textGrey
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg
                                    ),
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))
                            }

                            // Username Input
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        stringResource(id = R.string.username_label),
                                        color = textGrey
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                ),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                            // Password Input
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        stringResource(id = R.string.password_label),
                                        color = textGrey
                                    )
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = textGrey
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                ),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                            // Channels Input
                            OutlinedTextField(
                                value = channelCount,
                                onValueChange = { channelCount = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        stringResource(id = R.string.channels_label),
                                        color = textGrey
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                ),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_small))
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.element_spacing)))

                            // Stream Type Selection
                            Text(
                                stringResource(id = R.string.stream_type),
                                color = Color.White,
                                fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacer_micro))
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isMainStream = true }
                                ) {
                                    RadioButton(
                                        selected = isMainStream,
                                        onClick = { isMainStream = true },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = accentColor,
                                            unselectedColor = textGrey
                                        )
                                    )
                                    Text(stringResource(id = R.string.hd_view), color = Color.White)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isMainStream = false }
                                ) {
                                    RadioButton(
                                        selected = !isMainStream,
                                        onClick = { isMainStream = false },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = accentColor,
                                            unselectedColor = textGrey
                                        )
                                    )
                                    Text(stringResource(id = R.string.sd_view), color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing)))

                            // Connect Button
                            val primaryGradient = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(id = R.dimen.button_height))
                                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_large)))
                                    .background(primaryGradient)
                                    .clickable {
                                        val finalPort = port.toIntOrNull() ?: 554
                                        val finalChannels = channelCount.toIntOrNull() ?: 0

                                        if (name.isEmpty() || ip.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                fillRequiredFields,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@clickable
                                        }

                                        if (finalChannels <= 0) {
                                            Toast.makeText(
                                                context,
                                                "Please enter channelCount",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@clickable
                                        }

                                        val path = CameraUrlBuilder.buildPath(
                                            selectedBrand,
                                            1,
                                            isMainStream
                                        )

                                        val device = if (deviceToEdit != null) {
                                            deviceToEdit.copy(
                                                name = name,
                                                ip = ip,
                                                port = finalPort,
                                                username = username,
                                                password = password,
                                                path = path,
                                                brand = selectedBrand.name,
                                                mainStream = isMainStream,
                                                channelCount = finalChannels
                                            )
                                        } else {
                                            Device(
                                                id = "",
                                                name = name,
                                                ip = ip,
                                                port = finalPort,
                                                username = username,
                                                password = password,
                                                path = path,
                                                brand = selectedBrand.name,
                                                mainStream = isMainStream,
                                                channelCount = finalChannels
                                            )
                                        }

                                        cameraViewModel.saveRoomDevice(device)
                                        
                                        val message = if (deviceToEdit != null) {
                                            cameraUpdated
                                        } else {
                                            cameraAdded
                                        }
                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        if (finalChannels == 1) {
                                            onNavigateToPlayerScreen(
                                                ip,
                                                finalPort,
                                                username,
                                                password,
                                                path,
                                                name
                                            )
                                        } else {
                                            onNavigateToMultiChannel(
                                                ip,
                                                finalPort,
                                                username,
                                                password,
                                                finalChannels,
                                                isMainStream,
                                                selectedBrand.name
                                            )
                                        }
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (deviceToEdit != null) stringResource(id = R.string.update) else stringResource(
                                        id = R.string.connect
                                    ),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.bottom_nav_height))) // Space for banner
                        }
                    }
                }
            }
        }
    }
}
