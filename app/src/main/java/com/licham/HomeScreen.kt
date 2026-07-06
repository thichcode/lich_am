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

private val nguHanhNames = arrayOf("Mộc", "Mộc", "Hỏa", "Hỏa", "Thổ", "Thổ", "Kim", "Kim", "Thủy", "Thủy")

fun getNguHanh(canIndex: Int): String = nguHanhNames[canIndex % 10]

fun normalizeScore(score: Int): Int = (score + 50).coerceIn(0, 100)

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

    val canIndex = remember(dayCanChi) {
        when (dayCanChi.first) {
            "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
            "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
            else -> 0
        }
    }
    val chiIndex = remember(dayCanChi) {
        when (dayCanChi.second) {
            "Tý" -> 0; "Sửu" -> 1; "Dần" -> 2; "Mão" -> 3; "Thìn" -> 4; "Tỵ" -> 5
            "Ngọ" -> 6; "Mùi" -> 7; "Thân" -> 8; "Dậu" -> 9; "Tuất" -> 10; "Hợi" -> 11
            else -> 0
        }
    }

    val assessment = remember(date, lunar, dayCanChi) {
        if (lunar != null) {
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
        Spacer(modifier = Modifier.height(Spacing8))

        HeroSection(
            day = date.dayOfMonth,
            weekday = weekdayNames[date.dayOfWeek.value % 7],
            quote = quote
        )

        if (lunar != null && assessment != null) {
            Spacer(modifier = Modifier.height(Spacing16))
            ThreeInfoCards(
                assessment = assessment,
                lunarDay = lunar.day,
                lunarMonth = lunar.month,
                monthCanChi = monthCanChi?.let { CanChiCalculator.formatCanChi(it) } ?: "",
                yearCanChi = CanChiCalculator.formatCanChi(yearCanChi)
            )
        }

        if (assessment != null && (assessment.goodActivities.isNotEmpty() || assessment.badActivities.isNotEmpty())) {
            Spacer(modifier = Modifier.height(Spacing12))
            ActivitiesCard(assessment)
        }

        if (terms != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            TermCard(terms.first, terms.second)
        }

        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing12))
            EventCard(events)
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            InfoCard(
                canChi = dayCanChi,
                assessment = assessment,
                nguHanh = getNguHanh(canIndex),
                lunar = lunar
            )
        }

        Spacer(modifier = Modifier.height(Spacing24))
    }
}

@Composable
private fun HeroSection(day: Int, weekday: String, quote: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekday,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "$day",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Spacing8))
        Text(
            text = "\u201C$quote\u201D",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing8)
        )
    }
}

@Composable
private fun ThreeInfoCards(
    assessment: DayAssessment,
    lunarDay: Int,
    lunarMonth: Int,
    monthCanChi: String,
    yearCanChi: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacing8)
    ) {
        MiniHoursCard(
            title = "Giờ tốt",
            hours = assessment.goodHours,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        LunarDateCard(
            lunarDay = lunarDay,
            lunarMonth = lunarMonth,
            monthCanChi = monthCanChi,
            yearCanChi = yearCanChi,
            score = assessment.score,
            label = assessment.label,
            modifier = Modifier.weight(1f)
        )
        MiniHoursCard(
            title = "Giờ xấu",
            hours = assessment.badHours,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniHoursCard(
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
        Column(modifier = Modifier.padding(Spacing10)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(Spacing6))
            hours.take(3).forEach { hour ->
                Text(
                    text = "Giờ ${hour.chiName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = hour.timeRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LunarDateCard(
    lunarDay: Int,
    lunarMonth: Int,
    monthCanChi: String,
    yearCanChi: String,
    score: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val nhomColors = when {
        score > 0 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        score < 0 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(
            modifier = Modifier.padding(Spacing10).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$lunarDay",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "tháng $lunarMonth",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing4))
            Text(
                text = "$monthCanChi · $yearCanChi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing6))
            Surface(
                shape = RoundedCornerShape(Spacing8),
                color = nhomColors.first
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = nhomColors.second,
                    modifier = Modifier.padding(horizontal = Spacing10, vertical = Spacing4)
                )
            }
        }
    }
}

@Composable
private fun ActivitiesCard(assessment: DayAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16),
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
            }
            if (assessment.badActivities.isNotEmpty()) {
                if (assessment.goodActivities.isNotEmpty()) Spacer(modifier = Modifier.height(Spacing8))
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
                            text = "\u2013",
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
private fun TermCard(current: TermInfo, next: TermInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16),
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
                    text = "\u2022 ${current.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Spacing8))
                Text(
                    text = "từ ${current.date.dayOfMonth}/${current.date.monthValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing4))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\u2022 ${next.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Spacing8))
                Text(
                    text = "${next.date.dayOfMonth}/${next.date.monthValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventCard(events: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16),
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
                        text = "\u2726",
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
private fun InfoCard(
    canChi: Pair<String, String>,
    assessment: DayAssessment,
    nguHanh: String,
    lunar: LunarDate?
) {
    val nScore = normalizeScore(assessment.score)
    val scorePct = nScore.toFloat() / 100f
    val scoreColor = when {
        assessment.score > 0 -> MaterialTheme.colorScheme.secondary
        assessment.score < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    val hoangDaoTruc = setOf("Mãn", "Bình", "Định", "Chấp", "Thành", "Khai")
    val isHoangDao = assessment.truc in hoangDaoTruc
    val hoangDaoText = if (isHoangDao) "Hoàng Đạo" else "Hắc Đạo"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing16)) {
            Text(
                text = "Chi tiết",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing10))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoLabel("Can Chi", "Ngày ${CanChiCalculator.formatCanChi(canChi)}", Modifier.weight(1f))
                InfoLabel("Ngũ hành", nguHanh, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(Spacing8))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoLabel("Trực", assessment.truc, Modifier.weight(1f))
                InfoLabel("Nhị thập bát tú", assessment.tu, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(Spacing8))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoLabel("Hoàng đạo", hoangDaoText, Modifier.weight(1f))
                InfoLabel("Điểm", "${nScore}/100", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(Spacing10))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(5.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(scorePct)
                        .height(10.dp)
                        .background(scoreColor, RoundedCornerShape(5.dp))
                )
            }
        }
    }
}

@Composable
private fun InfoLabel(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

