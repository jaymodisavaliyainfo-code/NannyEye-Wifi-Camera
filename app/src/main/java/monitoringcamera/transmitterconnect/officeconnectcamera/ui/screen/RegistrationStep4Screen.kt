package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.outlined.Person
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.component.*

@Composable
fun RegistrationStep4Screen(
    viewModel: RegistrationViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current
    var isLoading by remember { mutableStateOf(false) }

    // Check for existing user profile and auto-forward if complete
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            isLoading = true
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    isLoading = false
                    if (document.exists()) {
                        val existingName = document.getString("name") ?: ""
                        val existingUsername = document.getString("username") ?: ""
                        
                        if (existingName.isNotEmpty()) viewModel.fullName = existingName
                        if (existingUsername.isNotEmpty()) viewModel.userName = existingUsername
                    }
                }
                .addOnFailureListener {
                    isLoading = false
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
            Text("Almost there", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Tell us who's behind the account.", fontSize = 14.sp, color = Color(0xFF9CAAC0).copy(alpha = 0.6f), textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))

            RegistrationHeader(currentStep = 3, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                PremiumLabel("FULL NAME")
                Spacer(modifier = Modifier.height(10.dp))
                PremiumGlassTextField(
                    value = viewModel.fullName,
                    onValueChange = { viewModel.fullName = it },
                    placeholder = "Your name",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(32.dp))

                PremiumLabel("USERNAME")
                Spacer(modifier = Modifier.height(10.dp))
                PremiumGlassTextField(
                    value = viewModel.userName,
                    onValueChange = { viewModel.userName = it },
                    placeholder = "username",
                    leadingIcon = Icons.Default.AlternateEmail
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This is how other household members will see you.",
                    fontSize = 12.sp,
                    color = Color(0xFF9CAAC0).copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                PremiumButton(
                    text = "Create account",
                    isLoading = isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        if (viewModel.fullName.isNotBlank() && viewModel.userName.isNotBlank()) {
                            isLoading = true
                            val currentUser = auth.currentUser
                            
                            if (currentUser != null) {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(viewModel.fullName)
                                    .build()
                                
                                currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val db = FirebaseFirestore.getInstance()
                                            val normalizedUsername = viewModel.userName.trim().lowercase()
                                            val userRef = db.collection("users").document(currentUser.uid)
                                            val usernameRef = db.collection("usernames").document(normalizedUsername)
                                            
                                            val internalEmail = currentUser.email ?: "${currentUser.phoneNumber}@nannyeye.io"
                                            
                                            db.runTransaction { transaction ->
                                                val snapshot = transaction.get(usernameRef)
                                                if (snapshot.exists() && snapshot.getString("uid") != currentUser.uid) {
                                                    throw Exception("Username already taken")
                                                }

                                                val userData = mutableMapOf(
                                                    "name" to viewModel.fullName,
                                                    "username" to normalizedUsername,
                                                    "profile_image" to (currentUser.photoUrl?.toString() ?: ""),
                                                    "email" to internalEmail,
                                                    "updatedAt" to FieldValue.serverTimestamp(),
                                                    "phone" to (currentUser.phoneNumber ?: ""),
                                                    "plan" to "free",
                                                )

                                                val userDoc = transaction.get(userRef)
                                                if (!userDoc.exists()) {
                                                    userData["createdAt"] = FieldValue.serverTimestamp()
                                                    transaction.set(userRef, userData)
                                                } else {
                                                    transaction.update(userRef, userData as Map<String, Any>)
                                                }
                                                
                                                transaction.set(usernameRef, mapOf("uid" to currentUser.uid))
                                            }.addOnSuccessListener {
                                                if (currentUser.email == null) {
                                                    currentUser.updateEmail(internalEmail)
                                                }

                                                isLoading = false
                                                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                                onSuccess()
                                            }.addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                isLoading = false
                                onSuccess()
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all details", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = viewModel.fullName.isNotBlank() && viewModel.userName.isNotBlank() && !isLoading
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
