package com.licham

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Red500 = Color(0xFFE53935)
val Red700 = Color(0xFFB71C1C)
val Gold = Color(0xFFF9A825)
val LightGold = Color(0xFFFFF9C4)
val LightRedBg = Color(0xFFFFCDD2)
val LightGreen = Color(0xFFC8E6C9)
val OffWhite = Color(0xFFFFF8E1)
val LunarGray = Color(0xFF757575)
val GoodGreen = Color(0xFF2E7D32)
val BadRed = Color(0xFFC62828)

private val LightColorScheme = lightColorScheme(
    primary = Red700,
    onPrimary = Color.White,
    primaryContainer = LightRedBg,
    secondary = Gold,
    onSecondary = Color.Black,
    background = OffWhite,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = BadRed,
    onError = Color.White
)

@Composable
fun LichAmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
