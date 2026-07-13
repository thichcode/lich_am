package com.licham

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalTime

val LocalThemeMode = staticCompositionLocalOf { mutableStateOf(ThemeMode.SYSTEM) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 115, 12)
        window.navigationBarColor = android.graphics.Color.WHITE

        setContent {
            val prefs = remember { getSharedPreferences("licham_prefs", MODE_PRIVATE) }
            val themeMode = remember {
                mutableStateOf(ThemeMode.entries[prefs.getInt("theme_mode", ThemeMode.SYSTEM.ordinal)])
            }

            LaunchedEffect(themeMode.value) {
                prefs.edit().putInt("theme_mode", themeMode.value.ordinal).apply()
            }

            val systemDark = isSystemInDarkTheme()
            var refreshKey by remember { mutableIntStateOf(0) }

            LaunchedEffect(themeMode.value) {
                if (themeMode.value == ThemeMode.SUNRISE_SUNSET) {
                    while (true) {
                        delay(60_000)
                        refreshKey++
                    }
                }
            }

            val isDark = remember(themeMode.value, systemDark, refreshKey) {
                when (themeMode.value) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> systemDark
                    ThemeMode.SUNRISE_SUNSET -> SunriseSunset.isDarkAt(LocalTime.now())
                }
            }

            CompositionLocalProvider(
                LocalThemeMode provides themeMode
            ) {
                LichAmTheme(darkTheme = isDark) {
                    WebViewHomeScreen()
                }
            }
        }
    }
}
