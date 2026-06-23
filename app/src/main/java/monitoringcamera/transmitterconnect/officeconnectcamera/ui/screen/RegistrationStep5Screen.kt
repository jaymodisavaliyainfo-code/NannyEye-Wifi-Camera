package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import monitoringcamera.transmitterconnect.officeconnectcamera.R
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.LuxuriousAmbientEffects
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.PremiumFloatingLogo

@Composable
fun RegistrationStep5Screen(
    viewModel: RegistrationViewModel,
    onTimeout: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CheckAnimation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(5000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
    ) {
        LuxuriousAmbientEffects()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Logo
            PremiumFloatingLogo()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All set",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Animated Checkmark
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                    .width(4.dp), // thickness of border simulation
                contentAlignment = Alignment.Center
            ) {
                // Circle border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent, CircleShape)
                        .scale(1f)
                ) {
                    // Use Canvas or just a border on Box
                }
                
                // Actual Check Icon
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                
                // Outer circle border
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Account created",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            val welcomeText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF9CAAC0).copy(alpha = 0.6f))) {
                    append("Welcome, ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF00CFFF), fontWeight = FontWeight.Bold)) {
                    append("@${viewModel.userName.ifBlank { "user" }}")
                }
                withStyle(style = SpanStyle(color = Color(0xFF9CAAC0).copy(alpha = 0.6f))) {
                    append(". Taking you to choose your plan...")
                }
            }

            Text(
                text = welcomeText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
