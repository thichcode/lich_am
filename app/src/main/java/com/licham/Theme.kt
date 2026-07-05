package com.licham

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
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

val SeniorLightColorScheme = lightColorScheme(
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
    background = Color(0xFF1A1A1A),
    onBackground = CardWhite,
    surface = Color(0xFF212121),
    onSurface = CardWhite,
    surfaceVariant = Color(0xFF323232),
    onSurfaceVariant = Color(0xFFB3B3B3),
    error = DeepRed,
    onError = CardWhite,
    outline = LightGrayDivider
)

val SeniorDarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkText,
    primaryContainer = LightGoldBg,
    onPrimaryContainer = DarkText,
    secondary = JadeGreen,
    onSecondary = CardWhite,
    secondaryContainer = LightGreenBg,
    onSecondaryContainer = DarkText,
    tertiary = DeepRed,
    onTertiary = CardWhite,
    tertiaryContainer = LightRedBg,
    onTertiaryContainer = CardWhite,
    background = Color(0xFF121212),
    onBackground = CardWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = CardWhite,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = LightGrayDivider,
    error = DeepRed,
    onError = CardWhite
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

// Spacing constants (dp)
val Spacing1 = 1.dp
val Spacing2 = 2.dp
val Spacing4 = 4.dp
val Spacing6 = 6.dp
val Spacing8 = 8.dp
val Spacing10 = 10.dp
val Spacing12 = 12.dp
val Spacing14 = 14.dp
val Spacing16 = 16.dp
val Spacing20 = 20.dp
val Spacing24 = 24.dp

@Composable
fun LichAmTheme(content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val isDarkMode = isSystemInDarkTheme()

    val screenScale = when {
        config.screenWidthDp < 360 -> 0.78f
        config.screenWidthDp < 400 -> 0.88f
        config.screenWidthDp < 480 -> 0.95f
        else -> 1.02f
    }

    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * screenScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = if (isDarkMode) SeniorDarkColorScheme else SeniorLightColorScheme,
            typography = SeniorTypography,
            shapes = SeniorShapes,
            content = content
        )
    }
}
