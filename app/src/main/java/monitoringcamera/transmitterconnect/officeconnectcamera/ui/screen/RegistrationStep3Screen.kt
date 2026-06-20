package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.*

@Composable
fun RegistrationStep3Screen(
    viewModel: RegistrationViewModel,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val hasMinLength = password.length >= 8
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val isPasswordValid = hasMinLength && hasDigit && hasLowerCase && hasUpperCase

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080C14))) {
        LuxuriousAmbientEffects()
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            PremiumFloatingLogo()
            Spacer(modifier = Modifier.height(28.dp))
            Text("Secure your account", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Choose a strong password. It encrypts\naccess to your cameras.", fontSize = 14.sp, color = Color(0xFF9CAAC0).copy(alpha = 0.6f), textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            RegistrationHeader(currentStep = 2, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                PremiumLabel("CREATE PASSWORD")
                Spacer(modifier = Modifier.height(10.dp))
                PremiumGlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    PasswordCriteriaItem("At least 8 characters", hasMinLength)
                    PasswordCriteriaItem("Contains a number", hasDigit)
                    PasswordCriteriaItem("Mixes upper & lower case", hasLowerCase && hasUpperCase)
                }

                Spacer(modifier = Modifier.height(32.dp))

                PremiumLabel("CONFIRM PASSWORD")
                Spacer(modifier = Modifier.height(10.dp))
                PremiumGlassTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                )

                Spacer(modifier = Modifier.height(40.dp))

                PremiumButton(
                    text = "Continue",
                    isLoading = isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        if (password == confirmPassword && isPasswordValid) {
                            isLoading = true
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                currentUser.updatePassword(password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            onNavigateToProfile()
                                        } else {
                                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                isLoading = false
                                onNavigateToProfile()
                            }
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please meet all password requirements", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isPasswordValid && password == confirmPassword && !isLoading
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
