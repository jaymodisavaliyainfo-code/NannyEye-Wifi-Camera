package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider

class RegistrationViewModel : ViewModel() {
    var email by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var fullPhoneNumber by mutableStateOf("")
    var selectedTab by mutableStateOf("EMAIL")
    
    var verificationId by mutableStateOf("")
    var resendToken by mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null)
    
    var fullName by mutableStateOf("")
    var userName by mutableStateOf("")
    
    var resendCooldown by mutableIntStateOf(0)
    var mobileResendCooldown by mutableIntStateOf(0)
    
    var verificationSent by mutableStateOf(false)

    fun reset() {
        email = ""
        phoneNumber = ""
        fullPhoneNumber = ""
        selectedTab = "EMAIL"
        verificationId = ""
        resendToken = null
        fullName = ""
        userName = ""
        resendCooldown = 0
        mobileResendCooldown = 0
        verificationSent = false
    }
}
