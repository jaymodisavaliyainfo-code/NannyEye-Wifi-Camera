package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alexvas.rtsp.widget.RtspStatusListener
import com.alexvas.rtsp.widget.RtspSurfaceView
import android.app.Activity
import android.content.pm.ActivityInfo

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import monitoringcamera.transmitterconnect.officeconnectcamera.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChannelScreen(
    ip: String,
    port: Int,
    username: String,
    password: String,
    channelCount: Int,
    mainStream: Boolean,
    brand: CameraUrlBuilder.Brand,
    onBack: () -> Unit,
    onNavigateToPlayer: (String, Int, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val columns = getColumnCount(channelCount)
    val channels = (1..channelCount).toList()

    // Keep screen on and immersive mode
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        val windowInsetsController = WindowCompat.getInsetsController(window!!, view)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            activity?.requestedOrientation = originalOrientation
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$ip  —  $channelCount channels",
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Spacer to balance navigation icon
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.icon_size_standard)))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E14),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.spacer_micro)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacer_micro)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacer_micro))
        ) {
            items(channels) { channelNumber ->
                ChannelItem(
                    ip = ip,
                    port = port,
                    username = username,
                    password = password,
                    channelNumber = channelNumber,
                    mainStream = mainStream,
                    brand = brand,
                    onNavigateToPlayer = onNavigateToPlayer
                )
            }
        }
    }
}

@Composable
fun ChannelItem(
    ip: String,
    port: Int,
    username: String,
    password: String,
    channelNumber: Int,
    mainStream: Boolean,
    brand: CameraUrlBuilder.Brand,
    onNavigateToPlayer: (String, Int, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var statusText by remember { mutableStateOf("Connecting...") }
    var isLoading by remember { mutableStateOf(true) }

    val path = CameraUrlBuilder.buildPath(brand, channelNumber, mainStream)
    val uri = buildUri(ip, port, path)

    Card(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.slider_height_large) + dimensionResource(id = R.dimen.icon_size_medium)) // Base 160 + 40 = 200
            .clickable {
                onNavigateToPlayer(
                    ip,
                    port,
                    username,
                    password,
                    path.replace("/", "|"), // Encoding slash for URL
                    "${brand.displayName} — CH $channelNumber"
                )
            },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_standard)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F26))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    RtspSurfaceView(ctx).apply {
                        init(uri, username, password, null, null)
                        setStatusListener(object : RtspStatusListener {
                            override fun onRtspStatusConnecting() {
                                statusText = "Connecting..."
                                isLoading = true
                            }

                            override fun onRtspStatusConnected() {
                                statusText = "Loading..."
                            }

                            override fun onRtspStatusDisconnecting() {}

                            override fun onRtspStatusDisconnected() {
                                statusText = "Disconnected"
                                isLoading = true
                            }

                            override fun onRtspStatusFailedUnauthorized() {
                                statusText = "Auth failed"
                                isLoading = false
                            }

                            override fun onRtspStatusFailed(message: String?) {
                                statusText = "No signal"
                                isLoading = false
                            }

                            override fun onRtspFirstFrameRendered() {
                                statusText = ""
                                isLoading = false
                            }

                            override fun onRtspFrameSizeChanged(width: Int, height: Int) {}
                        })
                        start(true, false, false)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Handle updates if needed
                },
                onRelease = { view ->
                    view.stop()
                }
            )

            // Channel Label
            Text(
                text = "CH $channelNumber",
                color = Color.White,
                fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(dimensionResource(id = R.dimen.spacer_small))
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(dimensionResource(id = R.dimen.spacer_micro)))
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_micro), vertical = dimensionResource(id = R.dimen.spacer_micro))
            )

            // Status and Loading
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.radius_large)),
                        color = Color(0xFFBBC6E2),
                        strokeWidth = dimensionResource(id = R.dimen.elevation_small)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                }
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getColumnCount(count: Int): Int {
    return when {
        count <= 2 -> 1
        count <= 4 -> 2
        count <= 9 -> 3
        else -> 4
    }
}

private fun buildUri(ip: String, port: Int, path: String): Uri {
    val builder = Uri.Builder()
        .scheme("rtsp")
        .encodedAuthority("$ip:$port")

    if (path.contains("?")) {
        val pathOnly = path.substring(0, path.indexOf("?"))
        val query = path.substring(path.indexOf("?") + 1)
        builder.encodedPath(pathOnly)
        for (param in query.split("&")) {
            val kv = param.split("=")
            if (kv.size == 2) {
                builder.appendQueryParameter(kv[0], kv[1])
            }
        }
    } else {
        builder.encodedPath(path)
    }

    return builder.build()
}
