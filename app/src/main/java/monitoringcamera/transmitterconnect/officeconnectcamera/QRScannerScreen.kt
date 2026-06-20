package monitoringcamera.transmitterconnect.officeconnectcamera

import android.Manifest
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(onBack: () -> Unit, onScanSuccess: (String) -> Unit, viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        hasCameraPermission = it
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
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
                                                    val json = org.json.JSONObject(value)
                                                    val scannedSessionId = json.getString("session_id")
                                                    val targetDeviceId = json.optString("device_id", "")

                                                    if (targetDeviceId.isNotEmpty()) {
                                                        // Register interest in SaveSessions before navigating
                                                        val database = FirebaseDatabase.getInstance()
                                                        val ref = database.getReference("SaveSessions")
                                                            .child(targetDeviceId)
                                                            .child(scannedSessionId)

                                                        val saveData = mapOf("status" to "Online")
                                                        ref.setValue(saveData)
                                                        ref.onDisconnect().removeValue()
                                                    }

                                                    onScanSuccess(scannedSessionId)
                                                } catch (e: Exception) {
                                                    // Fallback if not JSON
                                                    // For manual pairing code, we might not have the deviceId easily available here
                                                    // unless we search for it. For now, just navigate.
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
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(dimensionResource(id = R.dimen.button_height_large))
                .background(Color.Black)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = dimensionResource(id = R.dimen.spacer_small))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                text = "Add Device",
                color = Color.White,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() },
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Overlay Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.scanner_offset_top)))

            Text(
                text = "Scan QR Code",
                color = Color.White,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_h2).toSp() },
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))

            Text(
                text = "Position the QR code within the frame",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.icon_size_standard)))

            // Scanner Frame
            Box(
                modifier = Modifier.size(dimensionResource(id = R.dimen.qr_container_size)),
                contentAlignment = Alignment.Center
            ) {
                val strokeWidth = dimensionResource(id = R.dimen.spacer_micro)
                val cornerSize = dimensionResource(id = R.dimen.icon_size_medium)
                val radius = dimensionResource(id = R.dimen.radius_standard)

                // Focus Corners
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = strokeWidth.toPx()
                    val cornerSizePx = cornerSize.toPx()
                    val radiusPx = radius.toPx()

                    // Top Left
                    drawPath(
                        path = Path().apply {
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
                        path = Path().apply {
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
                        path = Path().apply {
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
                        path = Path().apply {
                            moveTo(size.width - cornerSizePx, size.height)
                            lineTo(size.width - radiusPx, size.height)
                            quadraticTo(size.width, size.height, size.width, size.height - radiusPx)
                            lineTo(size.width, size.height - cornerSizePx)
                        },
                        color = Color(0xFFBBC6E2),
                        style = Stroke(strokeWidthPx)
                    )
                }

                // Scanning Line Animation
                val infiniteTransition = rememberInfiniteTransition()
                val lineY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(dimensionResource(id = R.dimen.spacer_nano))
                        .offset(y = (dimensionResource(id = R.dimen.qr_container_size) * lineY) - (dimensionResource(id = R.dimen.qr_container_size) / 2))
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
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.icon_size_large)))

            // Flash Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    onClick = {
                        flashEnabled = !flashEnabled
                        cameraControl?.enableTorch(flashEnabled)
                    },
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large)),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.screen_padding))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                Text(
                    text = "FLASH",
                    color = Color.White,
                    fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = with(LocalDensity.current) { dimensionResource(id = R.dimen.letter_spacing_tight).toSp() }
                )
            }
        }
    }
}
