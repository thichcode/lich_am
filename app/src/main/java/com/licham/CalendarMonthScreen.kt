package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val lunarDate: String?,
    val canChi: Pair<String, String>?,
    val assessment: DayAssessment? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMonthScreen(
    yearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit
) {
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    if (selectedDay != null) {
        val day = selectedDay!!
        CalendarDayDetail(
            year = yearMonth.year,
            month = yearMonth.monthValue,
            dayValue = day.day,
            canChi = day.canChi,
            onBack = { selectedDay = null },
            onToday = {
                selectedDay = null
                onYearMonthChange(YearMonth.now())
            }
        )
    } else {
        CalendarGrid(
            yearMonth = yearMonth,
            onYearMonthChange = onYearMonthChange,
            onDayClick = { selectedDay = it }
        )
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit,
    onDayClick: (CalendarDay) -> Unit
) {
    val monthNames = remember {
        arrayOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )
    }

    val today = remember { LocalDate.now() }

    val days = remember(yearMonth) {
        val firstOfMonth = yearMonth.atDay(1)
        val lastOfMonth = yearMonth.atEndOfMonth()
        val startDayOfWeek = firstOfMonth.dayOfWeek.value % 7
        val totalDays = lastOfMonth.dayOfMonth
        val days = mutableListOf<CalendarDay>()
        for (i in 0 until startDayOfWeek) {
            days.add(
                CalendarDay(
                    day = -1, isCurrentMonth = false, isToday = false,
                    lunarDate = null, canChi = null
                )
            )
        }
        for (d in 1..totalDays) {
                val date = yearMonth.atDay(d)
                val lunar = LunarCalculator.solar2lunar(d, yearMonth.monthValue, yearMonth.year)
                val jd = lunar?.let { LunarCalculator.jdFromDate(d, yearMonth.monthValue, yearMonth.year) }
                val canChi = jd?.let { CanChiCalculator.getDayCanChi(it) }
                val assessment = if (lunar != null && canChi != null && jd != null) {
                    val canIndex = when (canChi.first) {
                        "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
                        "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
                        else -> 0
                    }
                    val chiIndex = when (canChi.second) {
                        "Tý" -> 0; "Sửu" -> 1; "Dần" -> 2; "Mão" -> 3; "Thìn" -> 4; "Tỵ" -> 5
                        "Ngọ" -> 6; "Mùi" -> 7; "Thân" -> 8; "Dậu" -> 9; "Tuất" -> 10; "Hợi" -> 11
                        else -> 0
                    }
                    GoodBadEngine.assessDay(lunar.day, lunar.month, canIndex, chiIndex, jd, yearMonth.monthValue)
                } else null
                days.add(
                    CalendarDay(
                        day = d,
                        isCurrentMonth = true,
                        isToday = date == today,
                        lunarDate = if (lunar != null && (d == 1 || d == 15 || d == totalDays)) {
                            "${lunar.day}"
                        } else null,
                        canChi = canChi,
                        assessment = assessment
                    )
                )
            }
        days
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(Spacing8))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onYearMonthChange(yearMonth.minusMonths(1)) }) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "Tháng trước")
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${monthNames[yearMonth.monthValue - 1]} ${yearMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val lunarMonthInfo = run {
                    val lunar = LunarCalculator.solar2lunar(1, yearMonth.monthValue, yearMonth.year)
                    if (lunar != null) {
                        val canchi = CanChiCalculator.getYearCanChi(lunar.year)
                        "Tháng ${lunar.month} năm ${CanChiCalculator.formatCanChi(canchi)}"
                    } else ""
                }
                if (lunarMonthInfo.isNotEmpty()) {
                    Text(
                        text = lunarMonthInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {
                onYearMonthChange(YearMonth.now())
            }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Hôm nay")
            }

            IconButton(onClick = { onYearMonthChange(yearMonth.plusMonths(1)) }) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = "Tháng sau")
            }
        }

        Spacer(modifier = Modifier.height(Spacing8))

        Row(modifier = Modifier.fillMaxWidth()) {
            val headers = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
            headers.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = Spacing4),
            horizontalArrangement = Arrangement.spacedBy(Spacing2),
            verticalArrangement = Arrangement.spacedBy(Spacing2)
        ) {
            items(days) { day ->
                CalendarDayCell(
                    day = day,
                    onClick = { if (day.isCurrentMonth) onDayClick(day) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit
) {
    if (day.day == -1) {
        Box(modifier = Modifier.size(width = 40.dp, height = 56.dp))
    } else {
        val bgColor = if (day.isToday)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface

        Column(
            modifier = Modifier
                .size(width = 40.dp, height = 56.dp)
                .clip(RoundedCornerShape(Spacing8))
                .background(bgColor)
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${day.day}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (day.isToday)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (day.lunarDate != null) {
                Text(
                    text = day.lunarDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
            }
            if (day.assessment != null) {
                val dotColor = when {
                    day.assessment!!.score > 0 -> MaterialTheme.colorScheme.secondaryContainer
                    day.assessment!!.score < 0 -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
                Spacer(modifier = Modifier.height(Spacing2))
                Box(
                    modifier = Modifier
                        .size(Spacing4)
                        .background(dotColor, RoundedCornerShape(Spacing2))
                )
            }
        }
    }
}

@Composable
private fun CalendarDayDetail(
    year: Int,
    month: Int,
    dayValue: Int,
    canChi: Pair<String, String>?,
    onBack: () -> Unit,
    onToday: () -> Unit
) {
    val date = remember { LocalDate.of(year, month, dayValue) }
    val weekdayNames = remember {
        arrayOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing4, vertical = Spacing4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Trở lại")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${'$'}{date.dayOfMonth} ${'$'}{weekdayNames[date.dayOfWeek.value % 7]}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onToday) {
                Text("Hôm nay")
            }
        }
        DayDetailContent(date = date)
    }
}

@Composable
private fun HoursCard(
    title: String,
    hours: List<HourInfo>,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing14)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(Spacing8))
            hours.take(4).forEach { hour ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = Spacing2)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Spacing8)
                            .background(color, RoundedCornerShape(Spacing4))
                    )
                    Spacer(modifier = Modifier.width(Spacing8))
                    Column {
                        Text(
                            text = "Giờ ${hour.chiName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = hour.timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitiesCard(assessment: DayAssessment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nên làm",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Điểm: ${assessment.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (assessment.goodActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing6))
                assessment.goodActivities.take(6).forEach { activity ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(Spacing24)
                        )
                        Text(
                            text = activity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing10))
            Text(
                text = "Nên tránh",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            if (assessment.badActivities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing6))
                assessment.badActivities.take(6).forEach { activity ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "–",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.width(Spacing24)
                        )
                        Text(
                            text = activity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
