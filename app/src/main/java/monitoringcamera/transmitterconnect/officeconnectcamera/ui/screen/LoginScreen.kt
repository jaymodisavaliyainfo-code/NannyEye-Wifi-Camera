package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import monitoringcamera.transmitterconnect.officeconnectcamera.R
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.LuxuriousAmbientEffects
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.PremiumButton
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.PremiumFloatingLogo
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.PremiumGlassTextField
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.PremiumLabel

@Composable
fun LoginScreen(
    viewModel: RegistrationViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToStep4: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    var emailOrMobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Google Sign-In Setup
    val webClientId = stringResource(id = R.string.default_web_client_id)

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(gso) { GoogleSignIn.getClient(activity, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            val user = authResult.result?.user
                            if (user != null) {
                                // Check if user already has a username in Firestore
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        isLoading = false
                                        if (document.exists() && !document.getString("username").isNullOrBlank()) {
                                            // User already complete, go to Main
                                            onNavigateToStep4()
                                        } else {
                                            // New user or incomplete, proceed to Step 4
                                            viewModel.fullName = user.displayName ?: ""
                                            viewModel.email = user.email ?: ""
                                            onNavigateToStep4()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        // Fallback: proceed to Step 4 with basic info
                                        viewModel.fullName = user.displayName ?: ""
                                        viewModel.email = user.email ?: ""
                                        onNavigateToStep4()
                                    }
                            } else {
                                isLoading = false
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Auth Failed: ${authResult.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google sign in failed: ${e.message}")
                Toast.makeText(context, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
    ) {
        // Luxurious Ambient Background Effects
        LuxuriousAmbientEffects()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Premium Floating Logo
            PremiumFloatingLogo()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "NannyEye",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    shadow = Shadow(
                        color = Color(0xFFBBC6E2).copy(alpha = 0.5f),
                        blurRadius = 15f
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Next-generation surveillance network.",
                fontSize = 14.sp,
                color = Color(0xFF9CAAC0).copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // EMAIL OR USERNAME LABEL
                PremiumLabel("EMAIL OR USERNAME")

                Spacer(modifier = Modifier.height(10.dp))

                PremiumGlassTextField(
                    value = emailOrMobile,
                    onValueChange = { emailOrMobile = it },
                    placeholder = "Email or Username",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PASSWORD LABEL ROW
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumLabel("PASSWORD", modifier = Modifier.padding(start = 0.dp))
                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBBC6E2),
                        modifier = Modifier.clickable { showForgotPasswordDialog = true }
                    )
                }

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

                Spacer(modifier = Modifier.height(32.dp))

                // Log In Button
                PremiumButton(
                    text = "Log In",
                    isLoading = isLoading,
                    onClick = {
                        val trimmedInput = emailOrMobile.trim()
                        if (trimmedInput.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            
                            val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedInput).matches()
                            
                            if (isEmail) {
                                // Direct login with email
                                auth.signInWithEmailAndPassword(trimmedInput, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            onNavigateToStep4()
                                        } else {
                                            Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                // Try login with username (case-insensitive)
                                val db = FirebaseFirestore.getInstance()
                                val normalizedUsername = trimmedInput.lowercase()
                                
                                db.collection("usernames")
                                    .document(normalizedUsername)
                                    .get()
                                    .addOnSuccessListener { usernameDoc ->
                                        if (usernameDoc.exists()) {
                                            val foundUid = usernameDoc.getString("uid")
                                            if (!foundUid.isNullOrBlank()) {
                                                // Find the email in 'users' collection using UID
                                                db.collection("users").document(foundUid)
                                                    .get()
                                                    .addOnSuccessListener { userDoc ->
                                                        // Fallback to dummy email if no real email is linked
                                                        val foundEmail = userDoc.getString("email")
                                                            .takeIf { !it.isNullOrBlank() } 
                                                            ?: "$foundUid@nannyeye.io"

                                                        auth.signInWithEmailAndPassword(foundEmail, password)
                                                            .addOnCompleteListener { task ->
                                                                isLoading = false
                                                                if (task.isSuccessful) {
                                                                    onLoginSuccess()
                                                                } else {
                                                                    Toast.makeText(context, "Password incorrect for '$trimmedInput'", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        Toast.makeText(context, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Account configuration error.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Username '$trimmedInput' not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Search Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Please enter both identifier and password", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // OR CONTINUE WITH Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Text(
                        text = "OR CONTINUE WITH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8EA3C0).copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign In with Google Glassy Button
                OutlinedButton(
                    onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF31353C).copy(alpha = 0.18f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account? ",
                    color = Color(0xFF9CAAC0),
                    fontSize = 14.sp
                )
                Text(
                    text = "Create a new account",
                    color = Color(0xFFBBC6E2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onCreateAccountClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resetStage by remember { mutableStateOf("IDENTIFIER") } // IDENTIFIER, OTP, NEW_PASSWORD
    
    // Phone OTP states
    var verificationId by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var linkedPhoneNumber by remember { mutableStateOf("") }

    // New Password states
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val activity = context as Activity

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A1931),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { 
            Text(
                text = when(resetStage) {
                    "OTP" -> "Verify OTP"
                    "NEW_PASSWORD" -> "Set New Password"
                    else -> "Reset Password"
                }, 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column {
                when (resetStage) {
                    "IDENTIFIER" -> {
                        Text(
                            "Enter your email or username to receive a reset link.",
                            fontSize = 14.sp,
                            color = Color(0xFF9CAAC0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            label = { Text("Email or Username") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFBBC6E2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFBBC6E2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    "OTP" -> {
                        Text(
                            "Enter the 6-digit code sent to ${linkedPhoneNumber.takeLast(4).padStart(linkedPhoneNumber.length, '*')}",
                            fontSize = 14.sp,
                            color = Color(0xFF9CAAC0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("6-Digit OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFBBC6E2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFBBC6E2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    "NEW_PASSWORD" -> {
                        Text(
                            "Set a new password for your account.",
                            fontSize = 14.sp,
                            color = Color(0xFF9CAAC0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFBBC6E2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFBBC6E2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (resetStage) {
                        "IDENTIFIER" -> {
                            val trimmedInput = identifier.trim()
                            if (trimmedInput.isNotBlank()) {
                                isLoading = true
                                val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedInput).matches()
                                
                                if (isEmail) {
                                    auth.sendPasswordResetEmail(trimmedInput)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            } else {
                                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    // Lookup username
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("usernames").document(trimmedInput.lowercase()).get()
                                        .addOnSuccessListener { doc ->
                                            if (doc.exists()) {
                                                val uid = doc.getString("uid")
                                                if (!uid.isNullOrEmpty()) {
                                                    db.collection("users").document(uid).get()
                                                        .addOnSuccessListener { userDoc ->
                                                            val email = userDoc.getString("email")
                                                            val phone = userDoc.getString("phone")
                                                            
                                                            if (!email.isNullOrBlank() && !email.endsWith("@phone.nannyeye.com")) {
                                                                // Account has a real email, use email reset
                                                                auth.sendPasswordResetEmail(email)
                                                                    .addOnCompleteListener { task ->
                                                                        isLoading = false
                                                                        if (task.isSuccessful) {
                                                                            Toast.makeText(context, "Reset link sent to your linked email!", Toast.LENGTH_LONG).show()
                                                                            onDismiss()
                                                                        } else {
                                                                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                            } else if (!phone.isNullOrBlank()) {
                                                                // No real email, but has phone - use OTP reset
                                                                linkedPhoneNumber = phone
                                                                val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
                                                                    .setPhoneNumber(phone)
                                                                    .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                                                                    .setActivity(activity)
                                                                    .setCallbacks(object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                                        override fun onVerificationCompleted(cred: com.google.firebase.auth.PhoneAuthCredential) {
                                                                            // Auto-verification handled if possible
                                                                        }
                                                                        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                                                                            isLoading = false
                                                                            Toast.makeText(context, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                        override fun onCodeSent(id: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                                                                            isLoading = false
                                                                            verificationId = id
                                                                            resetStage = "OTP"
                                                                            Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }).build()
                                                                com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options)
                                                            } else {
                                                                isLoading = false
                                                                Toast.makeText(context, "No reset method (email/phone) linked to this username.", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(context, "Account error.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Username not found.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                        "OTP" -> {
                            if (otpCode.length == 6) {
                                isLoading = true
                                val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, otpCode)
                                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        isLoading = false
                                        resetStage = "NEW_PASSWORD"
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        "NEW_PASSWORD" -> {
                            if (newPassword.length >= 8) {
                                isLoading = true
                                auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBBC6E2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = when(resetStage) {
                            "OTP" -> "Verify"
                            "NEW_PASSWORD" -> "Update Password"
                            else -> "Send"
                        }, 
                        color = Color.Black, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
