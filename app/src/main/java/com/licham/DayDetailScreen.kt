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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayDetailScreen(
    day: CalendarDay,
    onBack: () -> Unit
) {
    val assessment = remember(day) {
        if (day.lunarDate != null && day.canChi != null) {
            val canIndex = when (day.canChi.first) {
                "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
                "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
                else -> 0
            }
            val chiIndex = when (day.canChi.second) {
                "Tý" -> 0; "Sửu" -> 1; "Dần" -> 2; "Mão" -> 3; "Thìn" -> 4; "Tỵ" -> 5
                "Ngọ" -> 6; "Mùi" -> 7; "Thân" -> 8; "Dậu" -> 9; "Tuất" -> 10; "Hợi" -> 11
                else -> 0
            }
            GoodBadEngine.assessDay(day.lunarDate.day, day.lunarDate.month, canIndex, chiIndex)
        } else null
    }

    val quote = remember { QuoteProvider.getRandomQuote() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Chi tiết ngày",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                TextButton(onClick = onBack) {
                    Text("< Quay lại", color = Color.White, fontSize = 16.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Red700,
                titleContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SolarDateCard(day)
            Spacer(modifier = Modifier.height(12.dp))
            LunarDateCard(day)
            Spacer(modifier = Modifier.height(12.dp))
            CanChiCard(day)
            Spacer(modifier = Modifier.height(12.dp))

            if (assessment != null) {
                DayRatingCard(assessment)
                Spacer(modifier = Modifier.height(12.dp))
                GoodHoursCard(assessment)
                Spacer(modifier = Modifier.height(12.dp))
                ActivitiesCard(assessment)
            }

            Spacer(modifier = Modifier.height(12.dp))
            QuoteCard(quote)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    titleColor: Color = Red700,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SolarDateCard(day: CalendarDay) {
    val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )
    SectionCard("Dương lịch") {
        Text(
            text = "Ngày ${day.solarDay} ${monthNames[day.solarMonth - 1]} năm ${day.solarYear}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (day.isToday) {
            Text(
                text = "Hôm nay",
                color = Red500,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LunarDateCard(day: CalendarDay) {
    val lunar = day.lunarDate ?: return
    SectionCard("Âm lịch") {
        val monthName = LunarCalculator.getLunarMonthName(lunar.month, lunar.isLeap)
        Text(
            text = "Ngày ${lunar.day} $monthName năm ${lunar.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        val yearCanChi = CanChiCalculator.getYearCanChi(lunar.year)
        Text(
            text = "Năm ${CanChiCalculator.formatCanChi(yearCanChi)}",
            fontSize = 14.sp,
            color = LunarGray
        )
        when {
            lunar.day == 1 && lunar.month == 1 -> {
                Text(
                    text = "Tết Nguyên Đán",
                    color = Red500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            lunar.day == 15 -> {
                Text(
                    text = "Ngày Rằm (Trăng tròn)",
                    color = Red500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            lunar.day == 1 -> {
                Text(
                    text = "Mùng Một (Đầu tháng)",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CanChiCard(day: CalendarDay) {
    val lunar = day.lunarDate ?: return
    val yearCanChi = CanChiCalculator.getYearCanChi(lunar.year)
    val monthCanChi = CanChiCalculator.getMonthCanChi(lunar.month, CanChiCalculator.getYearCanIndex(lunar.year))
    val dayCanChi = day.canChi
    val hourCanChi = CanChiCalculator.getCanChiHour(12, CanChiCalculator.getDayCanChi(
        LunarCalculator.jdFromDate(day.solarDay, day.solarMonth, day.solarYear)
    ).first.let { can ->
        when (can) {
            "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
            "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
            else -> 0
        }
    })

    SectionCard("Can Chi") {
        if (dayCanChi != null) {
            InfoRow("Ngày", CanChiCalculator.formatCanChi(dayCanChi))
        }
        InfoRow("Tháng", CanChiCalculator.formatCanChi(monthCanChi))
        InfoRow("Năm", CanChiCalculator.formatCanChi(yearCanChi))
        InfoRow("Giờ Ngọ", CanChiCalculator.formatCanChi(hourCanChi))
    }
}

@Composable
private fun DayRatingCard(assessment: DayAssessment) {
    val color = if (assessment.isGood) GoodGreen else BadRed
    SectionCard(
        title = "Đánh giá ngày",
        titleColor = color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = assessment.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${assessment.score})",
                fontSize = 14.sp,
                color = LunarGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (assessment.isGood) "Ngày tốt, nên tiến hành các việc quan trọng" else "Ngày xấu, nên thận trọng khi làm việc lớn",
            fontSize = 13.sp,
            color = LunarGray
        )
    }
}

@Composable
private fun GoodHoursCard(assessment: DayAssessment) {
    SectionCard("Giờ Hoàng Đạo (Tốt)", GoodGreen) {
        assessment.goodHours.forEach { hour ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "Giờ ${hour.chiName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoodGreen,
                    modifier = Modifier.width(72.dp)
                )
                Text(
                    text = hour.timeRange,
                    fontSize = 14.sp,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = hour.reason,
                    fontSize = 12.sp,
                    color = LunarGray
                )
            }
        }
    }
}

@Composable
private fun ActivitiesCard(assessment: DayAssessment) {
    SectionCard(
        title = if (assessment.isGood) "Nên làm" else "Không nên làm"
    ) {
        val activities = if (assessment.isGood) assessment.goodActivities else assessment.badActivities
        activities.forEach { activity ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = if (assessment.isGood) "+" else "-",
                    color = if (assessment.isGood) GoodGreen else BadRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.width(20.dp)
                )
                Text(
                    text = activity,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: Quote) {
    SectionCard("Danh ngôn", Color(0xFF5D4037)) {
        Text(
            text = "❝${quote.text}❞",
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF5D4037)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "- ${quote.author}",
            fontSize = 13.sp,
            color = LunarGray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Red700
        )
    }
}
