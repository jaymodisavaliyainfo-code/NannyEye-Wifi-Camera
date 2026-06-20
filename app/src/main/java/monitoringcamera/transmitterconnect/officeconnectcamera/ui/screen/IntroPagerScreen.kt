package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.pager.rememberPagerState

@Composable
fun IntroPagerScreen(
    onFinish: () -> Unit,
) {
    val pagerState = rememberPagerState { 3 }

    OnboardingScreenTemplate(
        pagerState = pagerState,
        onFinish = onFinish,
        onSkip = onFinish,
    )
}
