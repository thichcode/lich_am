package com.licham

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.YearMonth

val LocalThemeMode = staticCompositionLocalOf { mutableStateOf(ThemeMode.SYSTEM) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 115, 12)
        window.navigationBarColor = android.graphics.Color.WHITE

        val quotesJson = assets.open("quotes.json").bufferedReader().use { it.readText() }
        val rulesJson = assets.open("rules.json").bufferedReader().use { it.readText() }
        val eventsJson = assets.open("events.json")

        QuoteProvider.loadQuotes(quotesJson)
        GoodBadEngine.loadRules(rulesJson)
        EventProvider.load(eventsJson)

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
                LocalThemeMode provides themeMode,
                LocalUseWebView provides remember { mutableStateOf(true) }
            ) {
                LichAmTheme(darkTheme = isDark) {
                    AppMain()
                }
            }
        }
    }
}

enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Hôm nay", Icons.Outlined.Home),
    Calendar("Lịch tháng", Icons.Outlined.CalendarMonth),
    GoodDays("Ngày tốt", Icons.Outlined.AutoAwesome),
    Prayers("Văn khấn", Icons.AutoMirrored.Outlined.MenuBook),
    Settings("Thêm", Icons.Outlined.MoreHoriz)
}

@Composable
fun AppMain() {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var calendarYearMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp)
            ) {
                AppTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .padding(horizontal = 3.dp, vertical = 4.dp)
                            .background(
                                color = if (selected) Color(0xFF08730C) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when (selectedTab) {
                AppTab.Home -> HomeScreen()
                AppTab.Calendar -> CalendarMonthScreen(
                    yearMonth = calendarYearMonth,
                    onYearMonthChange = { calendarYearMonth = it }
                )
                AppTab.GoodDays -> GoodDayScreen()
                AppTab.Prayers -> VanKhanScreen()
                AppTab.Settings -> SettingsScreen()
            }
        }
    }
}
