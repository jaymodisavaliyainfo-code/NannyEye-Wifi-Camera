package monitoringcamera.transmitterconnect.officeconnectcamera.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat

// Load Inter font (fallback to default if not available)

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF00BFA5), // teal
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF00695C),
    secondary = Color(0xFF4DB6AC),
    background = Color(0xFF121212),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)



@Composable
fun NannyEyeTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(
        colorScheme = colors,
        content = {
            // Gradient background wrapper
            val gradient = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
            )
            Box(
                modifier = Modifier
                    .background(gradient)
                    .fillMaxSize()
            ) {
                content()
            }
        }
    )
}
