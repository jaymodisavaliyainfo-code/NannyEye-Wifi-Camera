package monitoringcamera.transmitterconnect.officeconnectcamera

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val authListener = FirebaseAuth.AuthStateListener {
        _user.value = it.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }
    
    fun updateCurrentUser() {
        _user.value = auth.currentUser
    }
}
