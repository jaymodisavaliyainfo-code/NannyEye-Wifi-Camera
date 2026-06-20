package monitoringcamera.transmitterconnect.officeconnectcamera.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import monitoringcamera.transmitterconnect.officeconnectcamera.R

val interFontFamily = FontFamily(
    Font(R.font.inter24ptregular, FontWeight.Normal)
)

// Set of Material typography styles to start with
val Typography: Typography
    @Composable
    get() {
        val density = LocalDensity.current
        val defaultTextStyle = TextStyle(
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Normal
        )
        
        return Typography(
            displayLarge = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_h1).toSp() }),
            displayMedium = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_h2).toSp() }),
            displaySmall = defaultTextStyle,
            headlineLarge = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() }),
            headlineMedium = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }),
            headlineSmall = defaultTextStyle,
            titleLarge = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_title).toSp() }),
            titleMedium = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }),
            titleSmall = defaultTextStyle,
            bodyLarge = defaultTextStyle.copy(
                fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() },
                lineHeight = with(density) { dimensionResource(id = R.dimen.line_height_body).toSp() },
                letterSpacing = with(density) { dimensionResource(id = R.dimen.letter_spacing_normal).toSp() }
            ),
            bodyMedium = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_small).toSp() }),
            bodySmall = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_caption).toSp() }),
            labelLarge = defaultTextStyle,
            labelMedium = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_micro).toSp() }),
            labelSmall = defaultTextStyle.copy(fontSize = with(density) { dimensionResource(id = R.dimen.text_nano).toSp() })
        )
    }
