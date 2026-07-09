package com.licham

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Calendar

private val BlocGreen = Color(0xFF08680E)
private val BlocLightGreen = Color(0xFF4A9D18)
private val BlocRed = Color(0xFFD90000)

@Composable
fun HomeScreen() {
    val today = remember { LocalDate.now() }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.weight(1f)) {
            DayDetailContent(date = today)
        }
    }
}

@Composable
fun SelectedDateDetailScreen(date: LocalDate, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Trở lại",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            DayDetailContent(date = date)
        }
    }
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
    val weekday = weekdayNames[date.dayOfWeek.value % 7]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing16)
    ) {
        SolarWeekdayBlock(
            weekday = weekday.uppercase(),
            day = date.dayOfMonth,
            month = date.monthValue,
            year = date.year
        )
        QuoteCard(quote)

        if (lunar != null && assessment != null) {
            Spacer(modifier = Modifier.height(Spacing10))
            LunarDetailCard(
                lunarDay = lunar.day,
                lunarMonth = lunar.month,
                dayCanChi = dayCanChi,
                monthCanChi = monthCanChi,
                yearCanChi = yearCanChi,
                canIndex = canIndex,
                chiIndex = chiIndex
            )
            Spacer(modifier = Modifier.height(Spacing10))
            GoodBadHoursRow(assessment = assessment)
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            ActivitiesCard(assessment)
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            ClashAndDirectionsCard(chiIndex = chiIndex, canIndex = canIndex)
        }

        if (terms != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            TermSplitCard(terms.first, terms.second)
        }

        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing12))
            MockupEventsCard(date, lunar, events)
        }
    }
}

@Composable
private fun SolarWeekdayBlock(weekday: String, day: Int, month: Int, year: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weekday,
            color = BlocGreen,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$day",
            color = BlocRed,
            fontSize = 112.sp,
            lineHeight = 112.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-3).sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tháng $month $year",
            color = BlocGreen,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun QuoteCard(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text("“", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 42.sp, modifier = Modifier.align(Alignment.TopStart))
            Text("”", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 42.sp, modifier = Modifier.align(Alignment.TopEnd))
            Text(
                text = quote,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 34.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun LunarDetailCard(
    lunarDay: Int,
    lunarMonth: Int,
    dayCanChi: Pair<String, String>,
    monthCanChi: Pair<String, String>?,
    yearCanChi: Pair<String, String>,
    canIndex: Int,
    chiIndex: Int
) {
    val lunarMonthNames = remember {
        arrayOf("", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp")
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGoldBg),
        border = BorderStroke(2.dp, Color(0xFFD4A843))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌙", fontSize = 28.sp)
                Spacer(Modifier.width(Spacing10))
                Text("Âm lịch", color = Color(0xFF8B0000), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Spacing6))
            Text("$lunarDay", color = Color(0xFF8B0000), fontSize = 40.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
            Text("Tháng ${lunarMonthNames.getOrElse(lunarMonth) { lunarMonth.toString() }}", color = TextSecondary, fontSize = 18.sp)
            Spacer(Modifier.height(Spacing16))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CanChiLabel("Giờ", run {
                    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val hc = CanChiCalculator.getCanChiHour(h, canIndex)
                    "${hc.first} ${hc.second}"
                })
                CanChiLabel("Ngày", "${dayCanChi.first} ${dayCanChi.second}")
                CanChiLabel("Tháng", monthCanChi?.let { "${it.first} ${it.second}" } ?: "")
                CanChiLabel("Năm", "${yearCanChi.first} ${yearCanChi.second}")
            }
        }
    }
}

@Composable
private fun CanChiLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(Spacing4))
        Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GoodBadHoursRow(assessment: DayAssessment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing8)
    ) {
        GoodBadColumn(
            title = "GIỜ HOÀNG ĐẠO",
            hours = assessment.goodHours.take(6),
            accentColor = JadeGreen,
            modifier = Modifier.weight(1f)
        )
        GoodBadColumn(
            title = "GIỜ HẮC ĐẠO",
            hours = assessment.badHours.take(6),
            accentColor = DangerRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GoodBadColumn(title: String, hours: List<HourInfo>, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(title, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(Spacing12))
            hours.forEach { h ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        h.timeRange.replace("–", " - "),
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(h.chiName, color = TextPrimary, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ClashAndDirectionsCard(chiIndex: Int, canIndex: Int) {
    val chiNames = remember { arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi") }
    val clashChi = (chiIndex + 6) % 12
    val clashName = chiNames[clashChi]
    val hyThan = remember(canIndex) {
        when (canIndex) {
            0, 1 -> "Đông"; 2, 3 -> "Nam"; 4, 5 -> "Trung cung"; 6, 7 -> "Tây"; else -> "Bắc"
        }
    }
    val taiThan = remember(canIndex) {
        when (canIndex) {
            0, 1 -> "Đông Bắc"; 2, 3 -> "Chính Tây"; 4, 5 -> "Chính Đông"; 6, 7 -> "Chính Nam"; else -> "Tây Bắc"
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TUỔI XUNG KHẮC", color = DangerRed, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(Spacing8))
                    Text("Tuổi $clashName", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HƯỚNG TỐT", color = JadeGreen, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(Spacing8))
                    Text("Hỷ Thần $hyThan", color = TextPrimary, fontSize = 16.sp)
                    Text("Tài Thần $taiThan", color = TextPrimary, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivitiesCard(assessment: DayAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text("🎋", fontSize = 92.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp).alpha(0.35f))
            Column(modifier = Modifier.padding(18.dp)) {
                val gFallback = GoodBadEngine.getTrucGoodActivities(assessment.trucIdx).joinToString(", ")
                val bFallback = GoodBadEngine.getTrucBadActivities(assessment.trucIdx).joinToString(", ")
                ActivityLine(
                    icon = Icons.Outlined.CheckCircle,
                    title = "VIỆC NÊN LÀM",
                    text = assessment.goodActivities.joinToString(", ").ifBlank { gFallback },
                    color = BlocGreen
                )
                Spacer(modifier = Modifier.height(Spacing16))
                ActivityLine(
                    icon = Icons.Outlined.Cancel,
                    title = "VIỆC TRÁNH (KỴ)",
                    text = assessment.badActivities.joinToString(", ").ifBlank { bFallback },
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ActivityLine(icon: ImageVector, title: String, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(38.dp))
        Spacer(modifier = Modifier.width(Spacing16))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = color, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(Spacing6))
            Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun TermSplitCard(current: TermInfo, next: TermInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
            TermColumn("TIẾT KHÍ", current, Modifier.weight(1f))
            Box(modifier = Modifier.width(1.dp).height(96.dp).background(MaterialTheme.colorScheme.outline))
            TermColumn("TIẾT KHÍ TIẾP THEO", next, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TermColumn(title: String, term: TermInfo, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Outlined.Spa, contentDescription = null, tint = BlocGreen, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(Spacing10))
        Column {
            Text(title, color = BlocGreen, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(Spacing10))
            Text(term.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(Spacing6))
            Text("Bắt đầu: ${term.date.dayOfMonth.toString().padStart(2, '0')}/${term.date.monthValue.toString().padStart(2, '0')}/${term.date.year}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MockupEventsCard(date: LocalDate, lunar: LunarDate?, events: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = BlocLightGreen, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(Spacing16))
            Column(modifier = Modifier.weight(1f)) {
                Text("SỰ KIỆN - NGÀY LỄ", color = BlocGreen, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(Spacing12))
                events.forEachIndexed { index, event ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                        val dateText = if (index == 0) {
                            "• ${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
                        } else {
                            val lunarText = lunar?.let { " (${it.day.toString().padStart(2, '0')}/${it.month.toString().padStart(2, '0')} ÂL)" } ?: ""
                            "• ${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}$lunarText"
                        }
                        Text(dateText, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.width(134.dp))
                        Text(event, color = BlocGreen, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
