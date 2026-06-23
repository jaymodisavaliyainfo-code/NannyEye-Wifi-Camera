package monitoringcamera.transmitterconnect.officeconnectcamera

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.delay
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.DataItem
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationStep1Screen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationStep2Screen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationStep3Screen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationStep4Screen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationStep5Screen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.RegistrationViewModel
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.IntroPagerScreen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.LoginScreen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.PrivacyPolicyScreen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen.SignOutConfirmationScreen
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.theme.NannyEyeWiFiCameraMonitorTheme
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NannyEyeWiFiCameraMonitorTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val window = this.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && ev.pointerCount > 1) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}

@ExperimentalGetImage
@Composable
fun AppNavigation() {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val registrationViewModel: RegistrationViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    val user by authViewModel.user.collectAsState()

    LaunchedEffect(user) {
        if (user == null) {
            // Only navigate if we're not already on a login/intro/splash screen
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val authRoutes =
                listOf(
                    "login",
                    "intro",
                    "splash",
                    "phone_login",
                    "email_login",
                    "registration_step1"
                )
            if (currentRoute != null && currentRoute !in authRoutes) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    SideEffect {
        systemUiController.isNavigationBarVisible = false
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    val skipSplash = remember {
        activity?.intent?.getBooleanExtra("skip_splash", false) ?: false
    }

    val startDestination = remember {
        if (skipSplash) {
            if (authViewModel.isUserLoggedIn()) "main" else "login"
        } else {
            "splash"
        }
    }

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        detailScreen("splash") { _ ->
            SplashScreen(onTimeout = {
                val nextDest = if (authViewModel.isUserLoggedIn()) "main" else "login"

                navController.navigate(nextDest) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        detailScreen("intro") { _ ->
            IntroPagerScreen(
                onFinish = {
                    prefs.edit().putBoolean("intro_finished", true).apply()
                    navController.navigate("login") {
                        popUpTo("intro") { inclusive = true }
                    }
                })
        }
        detailScreen("registration_step1") { _ ->
            RegistrationStep1Screen(
                viewModel = registrationViewModel,
                onNavigateToOtp = { navController.navigate("registration_step2") },
                onNavigateToPassword = { navController.navigate("registration_step3") },
                onNavigateToProfile = { navController.navigate("registration_step4") },
                onBack = { navController.popBackStack() }
            )
        }
        detailScreen("registration_step2") { _ ->
            RegistrationStep2Screen(
                viewModel = registrationViewModel,
                onNavigateToPassword = { navController.navigate("registration_step3") },
                onBack = { navController.popBackStack() }
            )
        }
        detailScreen("registration_step3") { _ ->
            RegistrationStep3Screen(
                viewModel = registrationViewModel,
                onNavigateToProfile = { navController.navigate("registration_step4") },
                onBack = { navController.popBackStack() }
            )
        }
        detailScreen("registration_step4") { _ ->
            RegistrationStep4Screen(
                viewModel = registrationViewModel,
                onSuccess = {
                    navController.navigate("registration_step5") {
                        popUpTo("registration_step1") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        detailScreen("registration_step5") { _ ->
            RegistrationStep5Screen(
                viewModel = registrationViewModel,
                onTimeout = {
                    val nextDest = if (registrationViewModel.isGoogleLogin) "main" else "login"
                    navController.navigate(nextDest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        detailScreen("login") { _ ->
            LoginScreen(
                viewModel = registrationViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToStep4 = {
                    navController.navigate("registration_step4") {
                        popUpTo("login")
                    }
                },
                onCreateAccountClick = {
                    navController.navigate("registration_step1")
                }
            )
        }

        detailScreen("main") { _ ->
            MainScreen(navController, cameraViewModel, authViewModel)
        }
        detailScreen("sign_out_confirmation") { _ ->
            SignOutConfirmationScreen(
                onKeepMonitoring = { navController.popBackStack() },
                onSignOutConfirmed = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel,
                registrationViewModel = registrationViewModel
            )
        }
        detailScreen(
            route = "record_video"
        ) { _ ->
            RecordVideoScreen(onBack = { navController.popBackStack() })
        }
        detailScreen("monitor_details") { _ ->
            MonitorScreen(
                onBack = { navController.popBackStack() }, viewModel = cameraViewModel
            )
        }
        detailScreen("all_devices_records") { _ ->
            AllDevicesRecordsScreen(
                onBack = { navController.popBackStack() }, viewModel = cameraViewModel
            )
        }
        detailScreen("add_device") { _ ->
            AddDeviceScreen(
                onBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate("qr_scanner") },
                onNavigateToCameraView = { sessionId ->
                    navController.navigate("camera_view/$sessionId") {
                        popUpTo("add_device") { inclusive = true }
                    }
                },
                onNavigateToPlayerScreen = { ip, port, username, password, path, name ->
                    val encIp = Uri.encode(ip)
                    val encUser = Uri.encode(username)
                    val encPass = Uri.encode(password)
                    val encPath = Uri.encode(path)
                    val encName = Uri.encode(name)
                    navController.navigate("player/$encIp/$port/$encUser/$encPass/$encPath/$encName")
                },
                onNavigateToMultiChannel = { ip, port, username, password, channels, mainStream, brand ->
                    val encIp = Uri.encode(ip)
                    val encUser = Uri.encode(username)
                    val encPass = Uri.encode(password)
                    navController.navigate("multi_channel/$encIp/$port/$encUser/$encPass/$channels/$mainStream/$brand")
                },
                onNavigateToHelp = { navController.navigate("sentinel_guide") },
                cameraViewModel = cameraViewModel
            )
        }
        detailScreen("qr_scanner") { _ ->
            QRScannerScreen(
                onBack = { navController.popBackStack() },
                onScanSuccess = { sessionId ->
                    navController.navigate("viewer/$sessionId") {
                        popUpTo("qr_scanner") { inclusive = true }
                    }
                },
                viewModel = cameraViewModel
            )
        }
        detailScreen("viewer/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ViewerScreen(
                sessionId = sessionId, onBack = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }, viewModel = cameraViewModel
            )
        }
        detailScreen("camera_view/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CameraViewScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                viewModel = cameraViewModel
            )
        }
        detailScreen("multi_channel/{ip}/{port}/{username}/{password}/{channelCount}/{mainStream}/{brand}") { backStackEntry ->
            val ip = backStackEntry.arguments?.getString("ip") ?: ""
            val port = backStackEntry.arguments?.getString("port")?.toIntOrNull() ?: 554
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val channelCount =
                backStackEntry.arguments?.getString("channelCount")?.toIntOrNull() ?: 1
            val mainStream = backStackEntry.arguments?.getString("mainStream")?.toBoolean() ?: true
            val brandName = backStackEntry.arguments?.getString("brand") ?: "GENERIC"
            val brand = try {
                CameraUrlBuilder.Brand.valueOf(brandName)
            } catch (_: Exception) {
                CameraUrlBuilder.Brand.GENERIC
            }

            MultiChannelScreen(
                ip = ip,
                port = port,
                username = username,
                password = password,
                channelCount = channelCount,
                mainStream = mainStream,
                brand = brand,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { pip, pport, pusername, ppassword, ppath, pname ->
                    val encIp = Uri.encode(pip)
                    val encUser = Uri.encode(pusername)
                    val encPass = Uri.encode(ppassword)
                    val encPath = Uri.encode(ppath)
                    val encName = Uri.encode(pname)
                    navController.navigate("player/$encIp/$pport/$encUser/$encPass/$encPath/$encName")
                })
        }
        detailScreen("player/{ip}/{port}/{username}/{password}/{path}/{name}") { backStackEntry ->
            val ip = backStackEntry.arguments?.getString("ip") ?: ""
            val port = backStackEntry.arguments?.getString("port")?.toIntOrNull() ?: 554
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val path = backStackEntry.arguments?.getString("path") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""

            PlayerScreen(
                ip = ip,
                port = port,
                username = username,
                password = password,
                path = path,
                name = name,
                onBack = { navController.popBackStack() })
        }
        detailScreen("sentinel_guide") { _ ->
            SentinelGuideScreen(navController)
        }
        detailScreen("language_selection") { _ ->
            LanguageSelectionScreen(navController)
        }
        detailScreen("privacy_policy") { _ ->
            PrivacyPolicyScreen(navController)
        }
        detailScreen("linked_devices_screen") { _ ->
            LinkedDevicesScreen(navController)
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent, darkIcons = false
        )
    }

    val context = LocalContext.current
    val activity = context as Activity


    val onSurface = Color(0xFFDFE2EB)
    val onSurfaceVariant = Color(0xFFC5C6CD)
    val primary = Color(0xFFBBC6E2) // Needed for the glow

    val app = context.applicationContext as App
    app.fetchStartApps()
    app.fetchNativeAdApps()

    // --- ENTRANCE STATES ---
    var logoVisible by remember { mutableStateOf(false) }
    var typographyVisible by remember { mutableStateOf(false) }
    var securityPillVisible by remember { mutableStateOf(false) }
    var loaderActive by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")

    val loaderProgress by animateFloatAsState(
        targetValue = if (loaderActive) 1f else 0f,
        animationSpec = tween(3000, easing = FastOutSlowInEasing)
    )

    val isMobileAdsInitializeCalled = remember { AtomicBoolean(false) }

    val startFlow: () -> Unit = {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val hasInternet =
            activeNetwork != null && (activeNetwork.type == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE)

        if (hasInternet) {
            /*startAdsFlow(activity, isMobileAdsInitializeCalled) {
                loaderActive = true
            }*/
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra("skip_splash", true)
            })
            activity.finish()
        } else {
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra("skip_splash", true)
            })
            activity.finish()
            /*FacebookAds.getInstance(activity).AdsData(activity) {
                loaderActive = true
            }*/
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> startFlow() }

    LaunchedEffect(Unit) {
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        delay(100); logoVisible = true
        delay(200); typographyVisible = true
        delay(200)
        delay(200); securityPillVisible = true

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.getBoolean("install_pref_vd", false)) {
            FacebookAds.getInstance(activity).installCounter(activity)
            prefs.edit().putBoolean("install_pref_vd", true).apply()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startFlow()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startFlow()
        }
    }

    LaunchedEffect(loaderProgress) {
        if (loaderProgress == 1f) {
            delay(500)
            activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra("skip_splash", true)
            })
            activity.finish()
        }
    }

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, // Reduced from 0.2f
        targetValue = 0.3f,   // Reduced from 0.4f
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,  // Reduced expansion from 1.6f (0.6 -> 0.45)
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // --- UI LAYOUT ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14)) // Consistent dark background
    ) {
        // Broadcast Receiver (ACTION_CLOSE)
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == "ACTION_CLOSE") activity.finish()
                }
            }
            val filter = IntentFilter("ACTION_CLOSE")
            ContextCompat.registerReceiver(
                context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose { context.unregisterReceiver(receiver) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- Logo Block (w-40 h-40) ---
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated Glowing Shadow Layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .scale(glowScale)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primary.copy(alpha = glowAlpha),
                                        primary.copy(alpha = glowAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                )

                // Pure Logo (No background box, no border!)
                Image(
                    painter = painterResource(id = R.mipmap.icon512),
                    contentDescription = "NannyEye Brand Mark",
                    modifier = Modifier.fillMaxSize(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Typography ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NannyEye",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "INTELLIGENT CHILD MONITORING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceVariant,
                    letterSpacing = 2.4.sp,
                )
            }
        }

        // Loading Bar at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(1.5.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loaderProgress)
                        .background(Color.White.copy(alpha = 0.4f))
                )
            }
        }
    }
}

data class ParticleData(val x: Float, val y: Float, val size: Float, val speed: Float)

fun NavGraphBuilder.detailScreen(
    route: String,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { enterFromRight() },
        exitTransition = { exitToLeft() },
        popEnterTransition = { enterFromLeft() },
        popExitTransition = { exitToRight() }) { backStackEntry ->
        content(backStackEntry)
    }
}

// Transition Helper Functions
fun AnimatedContentTransitionScope<NavBackStackEntry>.enterFromRight(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.exitToLeft(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.enterFromLeft(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.exitToRight(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
    )
}

object AdsDataHolder {
    lateinit var adsData: DataItem
}

class GoogleMobileAdsConsentManager private constructor(context: Context) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager {
            return instance ?: synchronized(this) {
                instance ?: GoogleMobileAdsConsentManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun gatherConsent(
        activity: Activity, listener: OnConsentGatheringCompleteListener
    ) {
        val debugSettings = ConsentDebugSettings.Builder(activity).build()
        val params =
            ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
        consentInformation.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                activity
            ) { formError ->
                listener.consentGatheringComplete(formError)
            }
        }, { requestConsentError ->
            listener.consentGatheringComplete(requestConsentError)
        })
    }

    fun showPrivacyOptionsForm(
        activity: Activity, listener: ConsentForm.OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, listener)
    }
}

private fun startAdsFlow(
    activity: Activity, isMobileAdsInitializeCalled: AtomicBoolean, onComplete: () -> Unit
) {
    val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(activity)
    googleMobileAdsConsentManager.gatherConsent(
        activity, object : GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
            override fun consentGatheringComplete(consentError: FormError?) {
                if (consentError == null && googleMobileAdsConsentManager.canRequestAds()) {
                    if (isMobileAdsInitializeCalled.getAndSet(true)) {
                        onComplete()
                        return
                    }
                    MobileAds.initialize(activity) {
                        FacebookAds.getInstance(activity).AdsData(activity, onComplete)
                    }
                } else {
                    FacebookAds.getInstance(activity).AdsData(activity, onComplete)
                }
            }
        })
}
