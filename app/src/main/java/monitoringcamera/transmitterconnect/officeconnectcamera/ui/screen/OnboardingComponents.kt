package monitoringcamera.transmitterconnect.officeconnectcamera.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import monitoringcamera.transmitterconnect.officeconnectcamera.R

val OnboardingBackground = Color(0xFF0E1116)
val OnboardingPrimary = Color(0xFF77AEFF)
val OnboardingTextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun OnboardingScreenTemplate(
    pagerState: PagerState,
    onFinish: () -> Unit,
    onSkip: () -> Unit,
) {
    val pageIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    val titles = listOf(
        "Secure Access",
        "Encrypted Privacy",
        "Total Vigilance",
    )
    val descriptions = listOf(
        "Manage your entire security ecosystem from one central hub. Seamlessly sync devices for a truly unified vigilance experience.",
        "Your data is secured with bank-grade, end-to-end encryption protocols.",
        "Monitor your perimeter in real-time with ultra-low latency hardware acceleration."
    )

    val title = titles[pageIndex]
    val description = descriptions[pageIndex]
    val primaryButtonText = if (pageIndex == 2) "Get Started" else "Next"

    Scaffold(
        containerColor = OnboardingBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 50.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (pageIndex < 2) {
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "SKIP",
                            color = OnboardingTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (pageIndex == 2) {
                Image(
                    painter = painterResource(id = R.drawable.intro_3_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Graphic (ViewPager for Images)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        OnboardingGraphic(index)
                    }
                }

                // Content Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = description,
                            color = OnboardingTextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val isActive = index == pageIndex
                            val width by animateDpAsState(
                                targetValue = if (isActive) 32.dp else 8.dp,
                                label = "indicator-width"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(6.dp)
                                    .width(width)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isActive) Color.White else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Bottom Button
                    Button(
                        onClick = {
                            if (pageIndex < 2) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pageIndex + 1)
                                }
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFBBC6E2), OnboardingPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = primaryButtonText,
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingGraphic(pageIndex: Int) {
    val imageRes = when (pageIndex) {
        0 -> R.drawable.intro_1
        1 -> R.drawable.intro_2
        else -> R.drawable.intro_3
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
