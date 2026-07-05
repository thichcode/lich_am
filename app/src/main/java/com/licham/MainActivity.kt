package com.licham

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val quotesJson = assets.open("quotes.json").bufferedReader().use { it.readText() }
        val rulesJson = assets.open("good_bad_rules.json").bufferedReader().use { it.readText() }

        QuoteProvider.loadQuotes(quotesJson)
        GoodBadEngine.loadRules(rulesJson)

        setContent {
            LichAmTheme {
                AppNavigation()
            }
        }
    }
}

enum class AppScreen { Calendar, Detail }

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.Calendar) }
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    when (currentScreen) {
        AppScreen.Calendar -> {
            CalendarMonthScreen(
                onDayClick = { day ->
                    if (day.lunarDate != null) {
                        selectedDay = day
                        currentScreen = AppScreen.Detail
                    }
                }
            )
        }
        AppScreen.Detail -> {
            selectedDay?.let { day ->
                DayDetailScreen(
                    day = day,
                    onBack = {
                        currentScreen = AppScreen.Calendar
                    }
                )
            }
        }
    }
}
