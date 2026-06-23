package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.delay
import monitoringcamera.transmitterconnect.officeconnectcamera.R
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.*
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun RegistrationStep1Screen(
    viewModel: RegistrationViewModel,
    onNavigateToOtp: () -> Unit,
    onNavigateToPassword: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current
    var isLoading by remember { mutableStateOf(false) }

    val phoneUtil = remember { PhoneNumberUtil.getInstance() }
    val defaultCountry = remember {
        val iso = Locale.getDefault().country
        getCountries().find { it.code == iso } ?: Country("US", "+1", "United States", "🇺🇸")
    }
    var selectedCountry by remember { mutableStateOf(defaultCountry) }
    var isMobileValid by remember { mutableStateOf(false) }

    val isEmailValid = remember(viewModel.email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(viewModel.email).matches()
    }

    LaunchedEffect(viewModel.phoneNumber, selectedCountry) {
        try {
            val numberProto = phoneUtil.parse(viewModel.phoneNumber, selectedCountry.code)
            isMobileValid = phoneUtil.isValidNumber(numberProto)
            if (isMobileValid) {
                viewModel.fullPhoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
        } catch (e: Exception) {
            isMobileValid = false
        }
    }

    LaunchedEffect(viewModel.resendCooldown) {
        if (viewModel.resendCooldown > 0) {
            delay(1000)
            viewModel.resendCooldown -= 1
        }
    }

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
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        isLoading = false
                                        if (document.exists() && !document.getString("username").isNullOrBlank()) {
                                            viewModel.isGoogleLogin = true
                                            onNavigateToProfile()
                                        } else {
                                            viewModel.isGoogleLogin = true
                                            viewModel.fullName = user.displayName ?: ""
                                            viewModel.email = user.email ?: ""
                                            onNavigateToProfile()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        viewModel.isGoogleLogin = true
                                        viewModel.fullName = user.displayName ?: ""
                                        viewModel.email = user.email ?: ""
                                        onNavigateToProfile()
                                    }
                            } else {
                                isLoading = false
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Auth Failed: ${authResult.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
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
            Text("Create your account", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("One account for the apps, the web\ndashboard, and your plan.", fontSize = 14.sp, color = Color(0xFF9CAAC0).copy(alpha = 0.6f), textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            RegistrationHeader(currentStep = 0, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp).background(Color(0xFF141A28).copy(alpha = 0.2f), RoundedCornerShape(16.dp)).border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).padding(4.dp)
            ) {
                listOf("EMAIL", "MOBILE").forEach { tab ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .background(if (viewModel.selectedTab == tab) Brush.horizontalGradient(listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))) else SolidColor(Color.Transparent), RoundedCornerShape(14.dp))
                            .clickable { focusManager.clearFocus(); viewModel.selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (tab == "EMAIL") (if (viewModel.selectedTab == "EMAIL") Icons.Default.Email else Icons.Outlined.Email) else (if (viewModel.selectedTab == "MOBILE") Icons.Default.Smartphone else Icons.Outlined.Smartphone),
                                null, tint = if (viewModel.selectedTab == tab) Color.White else Color(0xFF9CAAC0).copy(alpha = 0.6f), modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tab, color = if (viewModel.selectedTab == tab) Color.White else Color(0xFF9CAAC0).copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PremiumLabel(if (viewModel.selectedTab == "EMAIL") "EMAIL ADDRESS" else "MOBILE NUMBER")
            Spacer(modifier = Modifier.height(10.dp))

            if (viewModel.selectedTab == "EMAIL") {
                PremiumGlassTextField(
                    value = viewModel.email,
                    onValueChange = { if (!viewModel.verificationSent) viewModel.email = it },
                    placeholder = "you@email.com",
                    leadingIcon = Icons.Outlined.Email,
                    enabled = !viewModel.verificationSent
                )
            } else {
                PremiumGlassTextField(
                    value = viewModel.phoneNumber,
                    onValueChange = { viewModel.phoneNumber = it },
                    placeholder = "Phone number",
                    leadingComposable = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CountryPicker(selectedCountry = selectedCountry, onCountrySelected = { selectedCountry = it })
                            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (viewModel.selectedTab == "EMAIL" && viewModel.verificationSent) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Verification link sent. Check your inbox.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        TextButton(onClick = { auth.currentUser?.sendEmailVerification(); viewModel.resendCooldown = 60 }, enabled = viewModel.resendCooldown == 0) {
                            Text(if (viewModel.resendCooldown > 0) "Resend in ${viewModel.resendCooldown}s" else "Resend", fontSize = 11.sp, color = if (viewModel.resendCooldown == 0) Color(0xFFBBC6E2) else Color.Gray)
                        }
                    }
                    TextButton(onClick = { viewModel.verificationSent = false; auth.signOut() }, contentPadding = PaddingValues(0.dp)) {
                        Text("Change Email Address", fontSize = 11.sp, color = Color(0xFFBBC6E2))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PremiumButton(
                text = if (viewModel.selectedTab == "EMAIL" && viewModel.verificationSent) "I have verified" else "Continue",
                isLoading = isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (viewModel.selectedTab == "EMAIL") {
                        if (viewModel.verificationSent) {
                            isLoading = true
                            auth.currentUser?.reload()?.addOnCompleteListener {
                                isLoading = false
                                if (auth.currentUser?.isEmailVerified == true) {
                                    onNavigateToPassword()
                                } else {
                                    Toast.makeText(context, "Please verify your email first", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else if (isEmailValid) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(viewModel.email, UUID.randomUUID().toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { vTask ->
                                            isLoading = false
                                            if (vTask.isSuccessful) { viewModel.verificationSent = true; viewModel.resendCooldown = 60 }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        if (isMobileValid) {
                            isLoading = true
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(viewModel.fullPhoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(activity)
                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                        auth.signInWithCredential(credential).addOnCompleteListener {
                                            isLoading = false
                                            if (it.isSuccessful) onNavigateToPassword()
                                        }
                                    }
                                    override fun onVerificationFailed(e: FirebaseException) {
                                        isLoading = false
                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                    override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                        isLoading = false
                                        viewModel.verificationId = id
                                        viewModel.resendToken = token
                                        viewModel.mobileResendCooldown = 60
                                        onNavigateToOtp()
                                    }
                                }).build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        } else { Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show() }
                    }
                },
                enabled = (if (viewModel.selectedTab == "EMAIL") isEmailValid else isMobileValid) && !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
            SocialAndFooter(onBackToLogin = onBack, googleSignInLauncher = googleSignInLauncher, googleSignInClient = googleSignInClient, focusManager = focusManager)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
