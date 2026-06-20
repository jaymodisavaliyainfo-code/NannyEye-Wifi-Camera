package monitoringcamera.transmitterconnect.officeconnectcamera.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class WindowSizeClass(
    val widthDp: Dp,
    val heightDp: Dp,
    val isCompactWidth: Boolean,
    val isMediumWidth: Boolean,
    val isExpandedWidth: Boolean,
    val isCompactHeight: Boolean,
    val isMediumHeight: Boolean,
    val isExpandedHeight: Boolean
)

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp.dp
    val heightDp = config.screenHeightDp.dp
    return remember(config) {
        WindowSizeClass(
            widthDp = widthDp,
            heightDp = heightDp,
            isCompactWidth = widthDp < 600.dp,
            isMediumWidth = widthDp in 600.dp..839.dp,
            isExpandedWidth = widthDp >= 840.dp,
            isCompactHeight = heightDp < 480.dp,
            isMediumHeight = heightDp in 480.dp..899.dp,
            isExpandedHeight = heightDp >= 900.dp
        )
    }
}

fun WindowSizeClass.isTablet(): Boolean = isMediumWidth || isExpandedWidth

fun Modifier.fillMaxWidthFraction(fraction: Float): Modifier = this.then(
    if (fraction < 1f) Modifier.fillMaxWidth(fraction) else Modifier.fillMaxWidth()
)

fun responsiveImageHeight(
    isCompactWidth: Boolean,
    baseHeight: Dp = 300.dp
): Dp {
    return if (isCompactWidth) {
        baseHeight
    } else {
        baseHeight * 1.5f
    }
}
