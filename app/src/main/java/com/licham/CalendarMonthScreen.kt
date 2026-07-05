package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDay(
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val lunarDate: LunarDate?,
    val canChi: Pair<String, String>?,
    val isToday: Boolean,
    val isCurrentMonth: Boolean
)

@Composable
fun CalendarMonthScreen(
    yearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit
) {
    val today = remember { LocalDate.now() }
    val monthNames = remember {
        arrayOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CalendarTopBar(
            title = "${monthNames[yearMonth.monthValue - 1]} ${yearMonth.year}",
            onPrev = { onYearMonthChange(yearMonth.minusMonths(1)) },
            onNext = { onYearMonthChange(yearMonth.plusMonths(1)) }
        )

        DayOfWeekHeader()

        val days = remember(yearMonth) {
            generateCalendarDays(yearMonth, today)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val chunked = days.chunked(7)
            items(chunked.size) { weekIndex ->
                val week = chunked[weekIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        CalendarDayCell(day = day)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CalendarTopBar(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Tháng trước",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Tháng sau",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val days = arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay) {
    val bgColor = when {
        !day.isCurrentMonth -> Color.Transparent
        day.isToday -> MaterialTheme.colorScheme.primary
        day.lunarDate?.day == 15 && day.isCurrentMonth -> MaterialTheme.colorScheme.tertiaryContainer
        day.lunarDate?.day == 1 && day.isCurrentMonth -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val textColor = when {
        day.isToday -> MaterialTheme.colorScheme.onPrimary
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .width(52.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (day.isToday) 2.dp else 0.dp,
                color = if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = day.isCurrentMonth) { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (day.isCurrentMonth) {
            Text(
                text = day.solarDay.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            val lunarText = when (day.lunarDate?.day) {
                1 -> "1"
                15 -> "15"
                else -> day.lunarDate?.day?.toString() ?: ""
            }
            if (lunarText.isNotEmpty()) {
                Text(
                    text = lunarText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (day.isToday)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else if (day.lunarDate?.day == 15)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun generateCalendarDays(yearMonth: YearMonth, today: LocalDate): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstOfMonth = yearMonth.atDay(1)
    val dow = firstOfMonth.dayOfWeek.value - 1
    val prevMonth = yearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    for (i in (daysInPrevMonth - dow + 1)..daysInPrevMonth) {
        val d = prevMonth.atDay(i)
        val lunar = LunarCalculator.solar2lunar(d.dayOfMonth, d.monthValue, d.year)
        val jd = LunarCalculator.jdFromDate(d.dayOfMonth, d.monthValue, d.year)
        val canchi = if (lunar != null) CanChiCalculator.getDayCanChi(jd) else null
        days.add(
            CalendarDay(
                solarDay = i,
                solarMonth = d.monthValue,
                solarYear = d.year,
                lunarDate = lunar,
                canChi = canchi,
                isToday = d == today,
                isCurrentMonth = false
            )
        )
    }

    val daysInMonth = yearMonth.lengthOfMonth()
    for (i in 1..daysInMonth) {
        val d = yearMonth.atDay(i)
        val lunar = LunarCalculator.solar2lunar(d.dayOfMonth, d.monthValue, d.year)
        val jd = LunarCalculator.jdFromDate(d.dayOfMonth, d.monthValue, d.year)
        val canchi = if (lunar != null) CanChiCalculator.getDayCanChi(jd) else null
        days.add(
            CalendarDay(
                solarDay = i,
                solarMonth = d.monthValue,
                solarYear = d.year,
                lunarDate = lunar,
                canChi = canchi,
                isToday = d == today,
                isCurrentMonth = true
            )
        )
    }

    val remainingDays = 7 - (days.size % 7)
    if (remainingDays < 7) {
        val nextMonth = yearMonth.plusMonths(1)
        for (i in 1..remainingDays) {
            val d = nextMonth.atDay(i)
            val lunar = LunarCalculator.solar2lunar(d.dayOfMonth, d.monthValue, d.year)
            val jd = LunarCalculator.jdFromDate(d.dayOfMonth, d.monthValue, d.year)
            val canchi = if (lunar != null) CanChiCalculator.getDayCanChi(jd) else null
            days.add(
                CalendarDay(
                    solarDay = i,
                    solarMonth = d.monthValue,
                    solarYear = d.year,
                    lunarDate = lunar,
                    canChi = canchi,
                    isToday = d == today,
                    isCurrentMonth = false
                )
            )
        }
    }

    return days
}
