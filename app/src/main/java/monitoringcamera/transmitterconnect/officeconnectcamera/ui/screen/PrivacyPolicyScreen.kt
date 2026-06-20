package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import monitoringcamera.transmitterconnect.officeconnectcamera.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val density = LocalDensity.current
    val textGrey = Color(0xFF9CA3AF)
    val darkBackground = Color(0xFF0E1116)
    val cardBackground = Color(0xFF1B1F26)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy Policy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        containerColor = darkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Privacy Policy",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                text = "June 05, 2026",
                color = textGrey,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            PrivacySection("1. INTRODUCTION", """
                The Wi-Fi Camera & Monitor App ("App", "we", "us", or "our") is designed to allow users to stream, view, and record live video and audio feeds over a local Wi-Fi network. This Privacy Policy applies to all users of the App and explains how we handle your data and device permissions.

                By installing and using this App, you agree to the practices described in this policy. If you do not agree, please discontinue use and uninstall the App.
            """.trimIndent())

            PrivacySection("2. PERMISSIONS WE REQUEST", """
                The App requests the following Android permissions to deliver core functionality:

                • CAMERA
                  Accesses your device camera to stream or broadcast live video over a local Wi-Fi connection.

                • RECORD_AUDIO
                  Captures audio from the device microphone during live streaming or recording sessions.

                • WRITE_EXTERNAL_STORAGE
                  Saves recorded video and audio files to your device's local storage (Android 9 and below).

                • READ_EXTERNAL_STORAGE
                  Reads previously saved recordings from your device's storage for playback within the App.

                • READ_MEDIA_VIDEO
                  Accesses video files on your device (Android 13+) to enable playback of saved recordings.
            """.trimIndent())

            PrivacySection("3. INFORMATION WE COLLECT", """
                The App operates primarily on your local Wi-Fi network. We collect the following types of data:

                • Live video and audio streams — processed locally on-device and transmitted only over your private local network.

                • Recorded video/audio files — stored exclusively on your device's internal or external storage.

                • Basic app usage data — such as app launch events or crash logs, which may be collected anonymously for stability improvements.

                IMPORTANT: We do not collect, upload, or transmit your camera footage, audio recordings, or media files to any external server or third party. All video and audio data remains on your local device and network.
            """.trimIndent())

            PrivacySection("4. HOW WE USE YOUR INFORMATION", """
                The data and permissions collected are used solely for the following purposes:

                • To enable real-time video and audio streaming over your local Wi-Fi network.
                • To allow recording and saving of camera footage to your device storage.
                • To enable playback of previously recorded video files within the App.
                • To diagnose crashes and improve app stability (anonymous crash data only).

                We do not use your camera, microphone, or media files for advertising, profiling, or any purpose beyond the core functionality described above.
            """.trimIndent())

            PrivacySection("5. DATA SHARING & THIRD PARTIES", """
                We do not sell, rent, or share your personal data or media content with third parties. The App may use the following third-party services for limited operational purposes:

                • Google Firebase — may be used for anonymous crash reporting and analytics.
                • Google Play Services — standard services provided by the Android platform.

                These services have their own privacy policies and operate independently. No video, audio, or identifiable user data is shared with them.
            """.trimIndent())

            PrivacySection("6. DATA SECURITY", """
                We implement appropriate technical measures to protect your data:

                • All video and audio streams are transmitted exclusively over your local Wi-Fi network and are not routed through external servers.
                • Recorded files are stored in app-specific or user-accessible directories on your device and protected by Android's built-in storage permissions model.
                • We recommend using a secure, password-protected Wi-Fi network when operating the App.
            """.trimIndent())

            PrivacySection("7. YOUR RIGHTS & CONTROLS", """
                You have full control over the App's permissions at all times:

                • You can grant or revoke any permission at any time via your device's Settings → Apps → [App Name] → Permissions.
                • Revoking a permission will disable the associated feature (e.g., revoking Camera access disables live streaming).
                • You can delete any recorded files directly from your device storage or within the App.
                • Uninstalling the App removes all app-specific data from your device.
            """.trimIndent())

            PrivacySection("8. CHILDREN'S PRIVACY", """
                This App is not directed at children under the age of 13. We do not knowingly collect personal data from children. If you believe a child has used this App and shared data, please contact us so we can take appropriate action.
            """.trimIndent())

            PrivacySection("9. CHANGES TO THIS POLICY", """
                We may update this Policy from time to time to reflect changes in the App's functionality or legal requirements. Any updates will be posted within the App or on the associated app store listing. Continued use of the App after changes are posted constitutes your acceptance of the revised policy.
            """.trimIndent())

            PrivacySection("10. CONTACT US", "") {
                val context = LocalContext.current
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "If you have any questions, concerns, or requests regarding this Privacy Policy or your data, please contact us at:",
                        color = Color(0xFFC5C6CD),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Email: ", color = Color(0xFFC5C6CD), fontSize = 14.sp)
                        Text(
                            text = "info@hacksec.ai",
                            color = Color(0xFF77AEFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:info@hacksec.ai")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Website: ", color = Color(0xFFC5C6CD), fontSize = 14.sp)
                        Text(
                            text = "hacksec.ai",
                            color = Color(0xFF77AEFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hacksec.ai"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Text(
                text = "This Privacy Policy was last updated on June 5, 2025. This document constitutes the complete privacy disclosure for the Wi-Fi Camera & Monitor App.",
                color = textGrey,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String, customContent: @Composable (() -> Unit)? = null) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            color = Color(0xFF1B1F26),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (customContent != null) {
                customContent()
            } else {
                Text(
                    text = content,
                    color = Color(0xFFC5C6CD),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
