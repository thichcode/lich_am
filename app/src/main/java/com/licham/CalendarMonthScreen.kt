package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onDayClick: (CalendarDay) -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        MonthHeader(
            yearMonth = currentYearMonth,
            onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
        )

        DayOfWeekHeader()

        val days = remember(currentYearMonth) {
            generateCalendarDays(currentYearMonth, today)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val chunked = days.chunked(7)
            items(chunked.size) { weekIndex ->
                val week = chunked[weekIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { day ->
                        DayCell(
                            day = day,
                            onClick = { onDayClick(day) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Red700)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevMonth) {
            Text("<", color = Color.White, fontSize = 20.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${monthNames[yearMonth.monthValue - 1]} ${yearMonth.year}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            val lunarYear = if (yearMonth.monthValue < 3) yearMonth.year else yearMonth.year
            val yearCanChi = CanChiCalculator.getYearCanChi(lunarYear)
            Text(
                text = yearCanChi.first + " " + yearCanChi.second,
                color = Gold,
                fontSize = 14.sp
            )
        }
        TextButton(onClick = onNextMonth) {
            Text(">", color = Color.White, fontSize = 20.sp)
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
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Red700
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    onClick: () -> Unit
) {
    val bgColor = when {
        !day.isCurrentMonth -> Color.Transparent
        day.isToday -> LightRedBg
        day.lunarDate?.day == 15 && day.isCurrentMonth -> LightGold
        day.lunarDate?.day == 1 && day.isCurrentMonth -> LightGreen.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    val borderColor = if (day.isToday) Red500 else Color.Transparent

    Column(
        modifier = Modifier
            .width(48.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(enabled = day.isCurrentMonth) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (day.isCurrentMonth) {
            Text(
                text = day.solarDay.toString(),
                fontSize = 15.sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (day.isToday) Red700 else Color.Black
            )
            val lunarText = when (day.lunarDate?.day) {
                1 -> "1"
                15 -> "15"
                else -> day.lunarDate?.day?.toString() ?: ""
            }
            if (lunarText.isNotEmpty()) {
                Text(
                    text = lunarText,
                    fontSize = 10.sp,
                    color = if (day.lunarDate?.day == 15) Red700 else LunarGray,
                    fontWeight = if (day.lunarDate?.day == 15) FontWeight.Bold else FontWeight.Normal
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
