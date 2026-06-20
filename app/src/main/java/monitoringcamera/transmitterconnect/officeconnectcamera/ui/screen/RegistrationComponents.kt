package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.graphics.BlurMaskFilter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationHeader(
    currentStep: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF141A28).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Step Indicators
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(4) { index ->
                val isActive = index == currentStep
                Box(
                    modifier = Modifier
                        .width(if (isActive) 22.dp else 6.dp)
                        .height(6.dp)
                        .drawBehind {
                            if (isActive) {
                                val frameworkPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    color = Color(0xFF00CFFF).toArgb()
                                    maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
                                }
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRoundRect(
                                        0f, 0f, size.width, size.height,
                                        3.dp.toPx(), 3.dp.toPx(),
                                        frameworkPaint
                                    )
                                }
                            }
                        }
                        .background(
                            if (isActive) Color(0xFF00CFFF) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(3.dp)
                        )
                )
                if (index < 3) Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
fun RegistrationStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val isActive = index == currentStep
            Box(
                modifier = Modifier
                    .width(if (isActive) 20.dp else 6.dp)
                    .height(6.dp)
                    .drawBehind {
                        if (isActive) {
                            val frameworkPaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = Color(0xFF00CFFF).toArgb()
                                maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
                            }
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawRoundRect(
                                    0f, 0f, size.width, size.height,
                                    3.dp.toPx(), 3.dp.toPx(),
                                    frameworkPaint
                                )
                            }
                        }
                    }
                    .background(
                        if (isActive) Color(0xFF00CFFF) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(3.dp)
                    )
            )
            if (index < 3) Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun SocialAndFooter(
    onBackToLogin: () -> Unit,
    googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            Text("OR CONTINUE WITH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8EA3C0).copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp), letterSpacing = 1.sp)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF313843).copy(alpha = 0.18f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign up with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        val context = LocalContext.current
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color(0xFF9CAAC0).copy(alpha = 0.6f))) { append("By creating an account you agree to our ") }
            pushStringAnnotation(tag = "TERMS", annotation = "terms")
            withStyle(style = SpanStyle(color = Color(0xFF00CFFF), fontWeight = FontWeight.Bold)) { append("Terms") }
            pop()
            withStyle(style = SpanStyle(color = Color(0xFF9CAAC0).copy(alpha = 0.6f))) { append(" & ") }
            pushStringAnnotation(tag = "POLICY", annotation = "policy")
            withStyle(style = SpanStyle(color = Color(0xFF00CFFF), fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
            pop()
        }
        ClickableText(text = annotatedString, style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center), onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset).firstOrNull()?.let { Toast.makeText(context, "Terms clicked", Toast.LENGTH_SHORT).show() }
            annotatedString.getStringAnnotations(tag = "POLICY", start = offset, end = offset).firstOrNull()?.let { Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show() }
        })
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color(0xFF9CAAC0), fontSize = 14.sp)
            Text("Log in", color = Color(0xFF00CFFF), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { focusManager.clearFocus(); onBackToLogin() })
        }
    }
}

@Composable
fun PasswordCriteriaItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(6.dp).background(if (isMet) Color(0xFF00CFFF) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(1.dp)).graphicsLayer { rotationZ = 45f })
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 13.sp, color = if (isMet) Color.White else Color(0xFF9CAAC0).copy(alpha = 0.6f))
    }
}
