package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    navController: androidx.navigation.NavController,
    viewModel: CameraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var mirrorCamera by remember { mutableStateOf(prefs.getBoolean("mirror_camera", false)) }
    var flipCamera by remember { mutableStateOf(prefs.getBoolean("flip_camera", false)) }
    var showClock by remember { mutableStateOf(prefs.getBoolean("show_clock", true)) }
    
    var motionDetection by remember { mutableStateOf(prefs.getBoolean("motion_detection", false)) }
    var sensitivity by remember { mutableStateOf(prefs.getFloat("motion_sensitivity", 0.5f)) }
    var aiDetection by remember { mutableStateOf(prefs.getBoolean("ai_detection", false)) }
    var alertSound by remember { mutableStateOf(prefs.getBoolean("alert_sound", true)) }

    // Apply preferences when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.applyPreferences()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Preferences",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1116))
            )
        },
        containerColor = Color(0xFF0E1116)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "DISPLAY & CAPTURE",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            PreferenceSwitchItem(
                title = "Mirror camera preview",
                subtitle = "Mirror the video feed for local preview.",
                checked = mirrorCamera,
                onCheckedChange = { 
                    mirrorCamera = it
                    prefs.edit().putBoolean("mirror_camera", it).apply()
                }
            )
            PreferenceSwitchItem(
                title = "Flip Monitored camera",
                subtitle = "Rotate the camera feed 180 degrees.",
                checked = flipCamera,
                onCheckedChange = { 
                    flipCamera = it
                    prefs.edit().putBoolean("flip_camera", it).apply()
                }
            )
            PreferenceSwitchItem(
                title = "Show Clock overlay",
                subtitle = "Display the current time on the recorded video.",
                checked = showClock,
                onCheckedChange = { 
                    showClock = it
                    prefs.edit().putBoolean("show_clock", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SECURITY & ALERTS",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Motion Detection Card with Slider
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B232D))
            ) {
                Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // Left Accent Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(3.dp)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp)
                            .background(Color(0xFF77AEFF), RoundedCornerShape(2.dp))
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CenterFocusStrong, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Motion Detection", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Enable AI-powered movement alerts.", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                            }
                            Switch(
                                checked = motionDetection,
                                onCheckedChange = { 
                                    motionDetection = it
                                    prefs.edit().putBoolean("motion_detection", it).apply()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF77AEFF),
                                    uncheckedThumbColor = Color(0xFFE5E7EB),
                                    uncheckedTrackColor = Color(0xFF374151)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("SENSITIVITY", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (sensitivity < 0.33f) "LOW" else if (sensitivity < 0.66f) "MEDIUM" else "HIGH", color = Color(0xFF77AEFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = sensitivity,
                            onValueChange = { 
                                sensitivity = it
                                prefs.edit().putFloat("motion_sensitivity", it).apply()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF77AEFF),
                                activeTrackColor = Color(0xFF77AEFF),
                                inactiveTrackColor = Color(0xFF374151)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Low", color = Color(0xFF6B7280), fontSize = 11.sp)
                            Text("Medium", color = Color(0xFF6B7280), fontSize = 11.sp)
                            Text("High", color = Color(0xFF6B7280), fontSize = 11.sp)
                        }
                    }
                }
            }

            PreferenceSwitchItem(
                title = "AI Person & Pet Detection",
                subtitle = "Identify and alert for people and pets specifically.",
                icon = Icons.Default.Pets,
                checked = aiDetection,
                showAccent = true,
                onCheckedChange = { 
                    aiDetection = it
                    prefs.edit().putBoolean("ai_detection", it).apply()
                }
            )

            PreferenceSwitchItem(
                title = "Alert sound",
                subtitle = "Play a notification sound when motion is detected.",
                icon = Icons.Default.Notifications,
                checked = alertSound,
                onCheckedChange = { 
                    alertSound = it
                    prefs.edit().putBoolean("alert_sound", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    viewModel.applyPreferences()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF77AEFF))
            ) {
                Text("Apply Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PreferenceSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    checked: Boolean,
    showAccent: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B232D))
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            if (showAccent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFF77AEFF), RoundedCornerShape(2.dp))
                )
            }
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF77AEFF),
                        uncheckedThumbColor = Color(0xFFE5E7EB),
                        uncheckedTrackColor = Color(0xFF374151)
                    )
                )
            }
        }
    }
}

