package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

data class DayInfo(
    val date: LocalDate,
    val lunarDate: LunarDate?,
    val assessment: DayAssessment?
)

@Composable
fun GoodDayScreen() {
    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    if (selectedDate != null) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing4, vertical = Spacing4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDate = null }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Trở lại")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Ngày ${selectedDate!!.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
            DayDetailContent(date = selectedDate!!)
        }
    } else {
        GoodDayGrid(currentMonth) { selectedDate = it }
    }
}

@Composable
private fun GoodDayGrid(currentMonth: YearMonth, onDayClick: (LocalDate) -> Unit) {
    val daysInMonth = remember(currentMonth) {
        val daysInMonth = currentMonth.lengthOfMonth()
        (1..daysInMonth).map { day ->
            val date = currentMonth.atDay(day)
            val lunar = LunarCalculator.solar2lunar(day, currentMonth.monthValue, currentMonth.year)
            val jd = LunarCalculator.jdFromDate(day, currentMonth.monthValue, currentMonth.year)
            val canChi = if (lunar != null) CanChiCalculator.getDayCanChi(jd) else null
            val assessment = if (lunar != null && canChi != null) {
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
                GoodBadEngine.assessDay(lunar.day, lunar.month, canIndex, chiIndex, jd, currentMonth.monthValue)
            } else null
            DayInfo(date, lunar, assessment)
        }
    }

    val monthNames = remember {
        arrayOf("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
            "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12")
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16, vertical = Spacing12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(Spacing12))
            Text(
                text = "${monthNames[currentMonth.monthValue - 1]} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing16, vertical = Spacing8),
            verticalArrangement = Arrangement.spacedBy(Spacing8)
        ) {
            items(daysInMonth) { dayInfo ->
                GoodBadDayCard(dayInfo = dayInfo, onClick = { onDayClick(dayInfo.date) })
            }
        }
    }
}

@Composable
private fun GoodBadDayCard(dayInfo: DayInfo, onClick: () -> Unit) {
    val score = dayInfo.assessment?.score ?: 0
    val isToday = dayInfo.date == LocalDate.now()

    val (cardColor, onCardColor, icon) = when {
        score > 0 -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Outlined.AutoAwesome
        )
        score < 0 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Outlined.Block
        )
        else -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Outlined.AutoAwesome
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) Spacing2 else 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onCardColor,
                modifier = Modifier.size(Spacing24)
            )
            Spacer(modifier = Modifier.width(Spacing12))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ngày ${dayInfo.date.dayOfMonth}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.width(Spacing8))
                        Surface(
                            shape = RoundedCornerShape(Spacing8),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Hôm nay",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = Spacing8, vertical = Spacing2)
                            )
                        }
                    }
                }
                if (dayInfo.lunarDate != null) {
                    Text(
                        text = "${dayInfo.lunarDate.day} tháng ${dayInfo.lunarDate.month} âm lịch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onCardColor.copy(alpha = 0.8f)
                    )
                }
            }
            if (dayInfo.assessment != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dayInfo.assessment.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor
                    )
                    Text(
                        text = "${dayInfo.assessment.score}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onCardColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
