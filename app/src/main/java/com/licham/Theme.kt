package com.licham

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DeepRed = Color(0xFFC62828)
val JadeGreen = Color(0xFF2E7D32)
val Gold = Color(0xFFF9A825)
val SoftBg = Color(0xFFF7F8FA)
val CardWhite = Color(0xFFFFFFFF)
val DarkText = Color(0xFF1A1A1A)
val GrayText = Color(0xFF666666)
val LightRedBg = Color(0xFFFFEBEE)
val LightGreenBg = Color(0xFFE8F5E9)
val LightGoldBg = Color(0xFFFFF8E1)
val LightGrayDivider = Color(0xFFEEEEEE)
val BrownText = Color(0xFF5D4037)

private val SeniorColorScheme = lightColorScheme(
    primary = DeepRed,
    onPrimary = CardWhite,
    primaryContainer = LightRedBg,
    onPrimaryContainer = DeepRed,
    secondary = JadeGreen,
    onSecondary = CardWhite,
    secondaryContainer = LightGreenBg,
    onSecondaryContainer = JadeGreen,
    tertiary = Gold,
    onTertiary = DarkText,
    tertiaryContainer = LightGoldBg,
    onTertiaryContainer = BrownText,
    background = SoftBg,
    onBackground = DarkText,
    surface = CardWhite,
    onSurface = DarkText,
    surfaceVariant = SoftBg,
    onSurfaceVariant = GrayText,
    error = DeepRed,
    onError = CardWhite,
    outline = LightGrayDivider
)

val SeniorTypography = Typography(
    displayLarge = TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Bold, lineHeight = 100.sp),
    displayMedium = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, lineHeight = 76.sp),
    displaySmall = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    headlineLarge = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Medium, lineHeight = 30.sp),
    titleMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp),
    titleSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    labelMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
)

val SeniorShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun LichAmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SeniorColorScheme,
        typography = SeniorTypography,
        shapes = SeniorShapes,
        content = content
    )
}
