package monitoringcamera.transmitterconnect.officeconnectcamera.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import monitoringcamera.transmitterconnect.officeconnectcamera.R

@Composable
fun PremiumLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF8EA3C0).copy(alpha = 0.5f),
        letterSpacing = 1.6.sp,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
fun PremiumGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    leadingComposable: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer {
                if (isFocused && enabled) {
                    scaleX = 1.015f
                    scaleY = 1.015f
                }
            }
            .drawBehind {
                if (isFocused && enabled) {
                    // Futuristic Neon Outer Glow (Only on the outside)
                    val frameworkPaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = Color(0xFFBBC6E2).toArgb()
                        maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.OUTER)
                    }
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height,
                            12.dp.toPx(), 12.dp.toPx(),
                            frameworkPaint
                        )
                    }
                }
            }
            .background(
                color = Color(0xFF313843).copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isFocused && enabled) Color(0xFFBBC6E2) else Color.White.copy(alpha = 0.12f),
                        if (isFocused && enabled) Color(0xFFBBC6E2).copy(alpha = 0.4f) else Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFF9CAAC0).copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            },
            modifier = Modifier.fillMaxSize(),
            leadingIcon = when {
                leadingComposable != null -> leadingComposable
                leadingIcon != null -> {
                    {
                        Icon(
                            leadingIcon,
                            contentDescription = null,
                            tint = if (isFocused && enabled) Color(0xFFBBC6E2) else Color(0xFF5C6B7F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                else -> null
            },
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            image,
                            contentDescription = null,
                            tint = Color(0xFF5C6B7F).copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFFBBC6E2)
            ),
            shape = RoundedCornerShape(32.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            textStyle = TextStyle(fontSize = 15.sp)
        )
    }
}

@Composable
fun PremiumButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled) listOf(Color(0xFFBBC6E2), Color(0xFF1B263B))
                            else listOf(Color(0xFFBBC6E2).copy(alpha = 0.5f), Color(0xFF1B263B).copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        enabled = enabled,
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "→", fontSize = 22.sp, fontWeight = FontWeight.Light, color = Color.White)
            }
        }
    }
}

@Composable
fun PremiumFloatingLogo() {
    Box(contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(id = R.mipmap.icon512),
            contentDescription = null,
            modifier = Modifier.size(150.dp).padding(14.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun LuxuriousAmbientEffects() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1A2E50).copy(alpha = 0.6f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.3f),
                radius = 420.dp.toPx()
            )
        )
    }
}
