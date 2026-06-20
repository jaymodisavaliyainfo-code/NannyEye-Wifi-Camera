package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelGuideScreen(navController: NavController) {
    val context = LocalContext.current
    val darkBackground = Color(0xFF0E1116)
    val cardBackground = Color(0xFF1B1F26)
    val textGrey = Color(0xFF9CA3AF)
    val blueAccent = Color(0xFF77AEFF)

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nanny Eye Guide",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(16.dp)
        ) {
            // Setup Assistant Banner
            Box(
                modifier = Modifier.fillMaxSize()
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.assistant_banner),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Phone Camera Section
            GuideSection(
                icon = Icons.Default.Smartphone,
                title = "How to Connect a\nPhone Camera",
                steps = listOf(
                    "Install NannyEye: WiFi Camera &amp; Monitor on the phone you want to use as a camera.",
                    "Open the app and select 'Switch to Camera' from the dashboard or settings.",
                    "A QR code or pairing ID will be displayed. Keep this screen active until paired."
                ),
                cardBackground = cardBackground,
                textGrey = textGrey,
                accentColor = blueAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monitor Section
            GuideSection(
                icon = Icons.Default.Monitor,
                title = "How to Connect a\nMonitor",
                steps = listOf(
                    "Open NannyEye: WiFi Camera &amp; Monitor on your primary phone (the one you'll use to watch).",
                    "Go to 'Devices' and tap the '+' icon or 'Add Device'.",
                    "Select 'Add Phone Camera' and scan the QR code displayed on the other phone.",
                    "Once paired, the live feed will automatically appear in your main dashboard."
                ),
                cardBackground = cardBackground,
                textGrey = textGrey,
                accentColor = Color(0xFF4A90E2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // IP Camera Section
            GuideSection(
                icon = Icons.Default.Videocam,
                title = "How to Connect an\nIP Camera",
                steps = listOf(
                    "Ensure your IP camera is powered on and connected to the same Wi-Fi network.",
                    "In the 'Add Device' menu, select 'IP Camera'.",
                    "Sentinel will search your network. Select your specific camera model from the detected list.",
                    "Enter the camera credentials (found on the device sticker or manual) to finalize the secure link."
                ),
                cardBackground = cardBackground,
                textGrey = textGrey,
                accentColor = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                "Still having trouble with your setup?",
                color = textGrey,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(if (isTablet) 0.35f else 0.55f)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                            ),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium))
                        )
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:info@hacksec.ai")
                            }
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CONTACT 24/7 SUPPORT",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GuideSection(
    icon: ImageVector,
    title: String,
    steps: List<String>,
    cardBackground: Color,
    textGrey: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (index + 1).toString(),
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        step,
                        color = textGrey,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
