package com.licham

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime

enum class ThemeMode { LIGHT, DARK, SYSTEM, SUNRISE_SUNSET }

object SunriseSunset {
    private const val LATITUDE = 21.0285
    private const val LONGITUDE = 105.8542

    fun isDarkAt(time: LocalTime, date: LocalDate = LocalDate.now()): Boolean {
        val dayOfYear = date.dayOfYear
        val declination = 23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (284.0 + dayOfYear)))
        val cosHourAngle = -Math.tan(Math.toRadians(LATITUDE)) * Math.tan(Math.toRadians(declination))
        if (cosHourAngle > 1.0 || cosHourAngle < -1.0) return false
        val hourAngle = Math.toDegrees(Math.acos(cosHourAngle))
        val sunriseHour = 12.0 - hourAngle / 15.0
        val sunsetHour = 12.0 + hourAngle / 15.0
        val currentMinutes = time.hour * 60 + time.minute
        val sunriseMinutes = (sunriseHour * 60).toInt()
        val sunsetMinutes = (sunsetHour * 60).toInt()
        return currentMinutes < sunriseMinutes || currentMinutes >= sunsetMinutes
    }
}

val DeepRed = Color(0xFFC62828)
val JadeGreen = Color(0xFF2E7D32)
val DangerRed = Color(0xFFD32F2F)
val Gold = Color(0xFFF9A825)
val WhiteBg = Color(0xFFFFFFFF)
val CardGray = Color(0xFFFAFAFA)
val TextPrimary = Color(0xFF202124)
val TextSecondary = Color(0xFF5F6368)
val DividerGray = Color(0xFFEAEAEA)
val LightRedBg = Color(0xFFFFEBEE)
val LightGreenBg = Color(0xFFE8F5E9)
val LightGoldBg = Color(0xFFFFF8E1)

private val LightColorScheme = lightColorScheme(
    primary = DeepRed,
    onPrimary = WhiteBg,
    primaryContainer = LightRedBg,
    onPrimaryContainer = DeepRed,
    secondary = JadeGreen,
    onSecondary = WhiteBg,
    secondaryContainer = LightGreenBg,
    onSecondaryContainer = JadeGreen,
    tertiary = Gold,
    onTertiary = TextPrimary,
    tertiaryContainer = LightGoldBg,
    onTertiaryContainer = Gold,
    background = WhiteBg,
    onBackground = TextPrimary,
    surface = CardGray,
    onSurface = TextPrimary,
    surfaceVariant = CardGray,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = WhiteBg,
    errorContainer = LightRedBg,
    onErrorContainer = DangerRed,
    outline = DividerGray
)

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = TextPrimary,
    primaryContainer = DeepRed,
    onPrimaryContainer = Gold,
    secondary = JadeGreen,
    onSecondary = WhiteBg,
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = JadeGreen,
    tertiary = Gold,
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFF3E2723),
    onTertiaryContainer = Gold,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF424242),
    error = DangerRed,
    onError = WhiteBg,
    errorContainer = Color(0xFF4E0000),
    onErrorContainer = DangerRed
)

val SeniorTypography = Typography(
    displayLarge = TextStyle(fontSize = 80.sp, fontWeight = FontWeight.Bold, lineHeight = 84.sp, letterSpacing = (-2).sp),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    titleSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
)

val SeniorShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

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
val Spacing32 = 32.dp
val Spacing40 = 40.dp

@Composable
fun LichAmTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val baseDensity = LocalDensity.current

    val diagDp = Math.sqrt(
        (config.screenWidthDp * config.screenWidthDp + config.screenHeightDp * config.screenHeightDp).toDouble()
    ).toFloat()
    val screenScale = when {
        diagDp < 580 -> 0.85f
        diagDp < 700 -> 0.93f
        diagDp < 860 -> 1.0f
        else -> 1.06f
    }

    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * screenScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = SeniorTypography,
            shapes = SeniorShapes,
            content = content
        )
    }
}
