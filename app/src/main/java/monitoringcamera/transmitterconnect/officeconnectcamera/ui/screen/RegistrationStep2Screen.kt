package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.*
import java.util.concurrent.TimeUnit

@Composable
fun RegistrationStep2Screen(
    viewModel: RegistrationViewModel,
    onNavigateToPassword: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.mobileResendCooldown) {
        if (viewModel.mobileResendCooldown > 0) {
            delay(1000)
            viewModel.mobileResendCooldown -= 1
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080C14))) {
        LuxuriousAmbientEffects()
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            PremiumFloatingLogo()
            Spacer(modifier = Modifier.height(28.dp))
            Text("Confirm it's you", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Enter the code we sent to verify your contact details.", fontSize = 14.sp, color = Color(0xFF9CAAC0).copy(alpha = 0.6f), textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            RegistrationHeader(currentStep = 1, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF9CAAC0).copy(alpha = 0.6f))) { append("We texted a 6-digit code to ") }
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append(viewModel.fullPhoneNumber) }
                },
                fontSize = 14.sp, textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    val isCurrent = index == otpCode.length || (index == 5 && otpCode.length == 6)
                    Box(
                        modifier = Modifier.size(width = 48.dp, height = 56.dp).clickable { focusRequester.requestFocus() }
                            .drawBehind {
                                if (isCurrent) {
                                    val frameworkPaint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        color = Color(0xFF00CFFF).toArgb()
                                        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
                                    }
                                    drawIntoCanvas { canvas -> canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, 12.dp.toPx(), 12.dp.toPx(), frameworkPaint) }
                                }
                            }
                            .background(Color(0xFF141A28).copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .border(1.dp, if (isCurrent) Color(0xFF00CFFF) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text(char, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }
            }
            
            Box(modifier = Modifier.size(1.dp).alpha(0f)) {
                OutlinedTextField(value = otpCode, onValueChange = { if (it.length <= 6 && it.all { it.isDigit() }) otpCode = it }, modifier = Modifier.focusRequester(focusRequester), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Didn't receive it? ", color = Color(0xFF9CAAC0).copy(alpha = 0.6f), fontSize = 13.sp)
                Text(if (viewModel.mobileResendCooldown > 0) "Resend in ${viewModel.mobileResendCooldown}s" else "Resend", color = if (viewModel.mobileResendCooldown == 0) Color(0xFF00CFFF) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable(enabled = viewModel.mobileResendCooldown == 0) { 
                    if (viewModel.mobileResendCooldown == 0) {
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(viewModel.fullPhoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(p0: PhoneAuthCredential) {}
                                override fun onVerificationFailed(p0: FirebaseException) {}
                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    viewModel.verificationId = id
                                    viewModel.resendToken = token
                                    viewModel.mobileResendCooldown = 60
                                    Toast.makeText(context, "OTP Resent", Toast.LENGTH_SHORT).show()
                                }
                            })
                            .apply { viewModel.resendToken?.let { setForceResendingToken(it) } }
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }
                })
            }

            Spacer(modifier = Modifier.height(40.dp))

            PremiumButton(
                text = "Verify",
                isLoading = isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (otpCode.length == 6) {
                        isLoading = true
                        val credential = PhoneAuthProvider.getCredential(viewModel.verificationId, otpCode)
                        auth.signInWithCredential(credential).addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onNavigateToPassword()
                            } else {
                                Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = otpCode.length == 6 && !isLoading
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
