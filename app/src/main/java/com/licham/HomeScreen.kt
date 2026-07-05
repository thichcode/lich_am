package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun HomeScreen() {
    val today = remember { LocalDate.now() }
    DayDetailContent(date = today)
}

@Composable
fun DayDetailContent(date: LocalDate) {
 val lunar = remember(date) {
        LunarCalculator.solar2lunar(date.dayOfMonth, date.monthValue, date.year)
    }
    val jd = remember(date) {
        LunarCalculator.jdFromDate(date.dayOfMonth, date.monthValue, date.year)
    }
    val dayCanChi = remember(date) { CanChiCalculator.getDayCanChi(jd) }

    val yearCanChi = remember(lunar) {
        CanChiCalculator.getYearCanChi(lunar?.year ?: date.year)
    }
    val yearCanIndex = remember(lunar) {
        CanChiCalculator.getYearCanIndex(lunar?.year ?: date.year)
    }
    val monthCanChi = remember(lunar, yearCanIndex) {
        if (lunar != null) CanChiCalculator.getMonthCanChi(lunar.month, yearCanIndex) else null
    }

    val weekdayNames = remember {
        arrayOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    }

    val assessment = remember(date, lunar, dayCanChi) {
        if (lunar != null) {
            val canIndex = when (dayCanChi.first) {
                "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
                "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
                else -> 0
            }
            val chiIndex = when (dayCanChi.second) {
                "Tý" -> 0; "Sửu" -> 1; "Dần" -> 2; "Mão" -> 3; "Thìn" -> 4; "Tỵ" -> 5
                "Ngọ" -> 6; "Mùi" -> 7; "Thân" -> 8; "Dậu" -> 9; "Tuất" -> 10; "Hợi" -> 11
                else -> 0
            }
            GoodBadEngine.assessDay(lunar.day, lunar.month, canIndex, chiIndex, jd, date.monthValue)
        } else null
    }

    val terms = remember(date) { TietKhiCalculator.getCurrentAndNext(date) }

    val events = remember(date, lunar) {
        if (lunar != null) {
            EventProvider.getTodayEvents(date.dayOfMonth, date.monthValue, lunar.day, lunar.month)
        } else emptyList()
    }

    val quote = remember(date) { QuoteProvider.getRandomQuote().text }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(Spacing10))

        HeroDateSection(
            day = date.dayOfMonth,
            weekday = weekdayNames[date.dayOfWeek.value % 7],
            lunarDay = lunar?.day,
            lunarMonth = lunar?.month,
            yearCanChi = yearCanChi,
            monthCanChi = monthCanChi,
            assessment = assessment,
            canChi = dayCanChi,
            truc = assessment?.truc,
            tu = assessment?.tu
        )

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing14))
            HoursRow(assessment)
        }

        if (terms != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            TermCard(terms.first, terms.second)
        }

        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing12))
            EventCard(events)
        }

        if (assessment != null && (assessment.goodActivities.isNotEmpty() || assessment.badActivities.isNotEmpty())) {
            Spacer(modifier = Modifier.height(Spacing12))
            ActivitiesCard(assessment)
        }

        Spacer(modifier = Modifier.height(Spacing12))
        QuoteCard(quote)

        Spacer(modifier = Modifier.height(Spacing24))
    }
}

@Composable
private fun HeroDateSection(
    day: Int,
    weekday: String,
    lunarDay: Int?,
    lunarMonth: Int?,
    yearCanChi: Pair<String, String>,
    monthCanChi: Pair<String, String>?,
    assessment: DayAssessment?,
    canChi: Pair<String, String>,
    truc: String?,
    tu: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            letterSpacing = (-2).sp
        )

        Text(
            text = weekday,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (lunarDay != null) {
            Spacer(modifier = Modifier.height(Spacing6))
            Text(
                text = "$lunarDay",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                letterSpacing = (-1).sp
            )

            if (lunarMonth != null && monthCanChi != null) {
                Spacer(modifier = Modifier.height(Spacing4))
                Text(
                    text = "Tháng ${CanChiCalculator.formatCanChi(monthCanChi)} năm ${CanChiCalculator.formatCanChi(yearCanChi)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing2))
        Text(
            text = "Ngày ${CanChiCalculator.formatCanChi(canChi)}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (truc != null && tu != null) {
            Text(
                text = "Trực $truc · Tú $tu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            val (pillColor, onPillColor) = when {
                assessment.score > 0 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                assessment.score < 0 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            }
            Surface(
                shape = RoundedCornerShape(Spacing12),
                color = pillColor
            ) {
                Text(
                    text = assessment.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onPillColor,
                    modifier = Modifier.padding(horizontal = Spacing20, vertical = Spacing8)
                )
            }
        }
    }
}

@Composable
private fun HoursRow(assessment: DayAssessment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16),
        horizontalArrangement = Arrangement.spacedBy(Spacing12)
    ) {
        HoursCard(
            title = "Giờ tốt",
            hours = assessment.goodHours,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        HoursCard(
            title = "Giờ xấu",
            hours = assessment.badHours,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TermCard(current: TermInfo, next: TermInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing16)) {
            Text(
                text = "Tiết khí",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing6))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "• ${current.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Spacing8))
                Text(
                    text = "từ ${current.date.dayOfMonth}/${current.date.monthValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing4))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "• ${next.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Spacing8))
                Text(
                    text = "${next.date.dayOfMonth}/${next.date.monthValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventCard(events: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing16)) {
            events.forEach { event ->
                Row(
                    modifier = Modifier.padding(vertical = Spacing2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✦",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.width(Spacing24)
                    )
                    Text(
                        text = event,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
            if (assessment.goodActivities.isNotEmpty()) {
                Text(
                    text = "Nên làm",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Spacing6))
                assessment.goodActivities.forEach { activity ->
                    Row(
                        modifier = Modifier.padding(vertical = Spacing2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            text = activity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing8))
            }
            if (assessment.badActivities.isNotEmpty()) {
                Text(
                    text = "Nên tránh",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing6))
                assessment.badActivities.forEach { activity ->
                    Row(
                        modifier = Modifier.padding(vertical = Spacing2),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "–",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.width(22.dp)
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

@Composable
private fun QuoteCard(quote: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Text(
            text = "“$quote”",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Spacing16)
        )
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
        Column(modifier = Modifier.padding(Spacing12)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(Spacing6))
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
