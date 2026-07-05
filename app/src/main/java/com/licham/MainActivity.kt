package com.licham

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val quotesJson = assets.open("quotes.json").bufferedReader().use { it.readText() }
        val rulesJson = assets.open("rules.json").bufferedReader().use { it.readText() }
        val eventsJson = assets.open("events.json")

        QuoteProvider.loadQuotes(quotesJson)
        GoodBadEngine.loadRules(rulesJson)
        EventProvider.load(eventsJson)

        setContent {
            LichAmTheme {
                AppMain()
            }
        }
    }
}

enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Hôm nay", Icons.Outlined.Home),
    Calendar("Lịch", Icons.Outlined.CalendarMonth),
    GoodDays("Ngày đẹp", Icons.Outlined.AutoAwesome),
    News("Tin tức", Icons.AutoMirrored.Outlined.Article),
    Settings("Khác", Icons.Outlined.MoreHoriz)
}

@Composable
fun AppMain() {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var calendarYearMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = Spacing2,
                modifier = Modifier.height(72.dp)
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                AppTab.Home -> HomeScreen()
                AppTab.Calendar -> CalendarMonthScreen(
                    yearMonth = calendarYearMonth,
                    onYearMonthChange = { calendarYearMonth = it }
                )
                AppTab.GoodDays -> GoodDayScreen()
                AppTab.News -> NewsScreen()
                AppTab.Settings -> SettingsScreen()
            }
        }
    }
}
