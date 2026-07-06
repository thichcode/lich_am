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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
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

private val BlocGreen = Color(0xFF08680E)
private val BlocLightGreen = Color(0xFF4A9D18)
private val BlocRed = Color(0xFFD90000)

@Composable
fun HomeScreen() {
    val today = remember { LocalDate.now() }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HomeHeader(date = today)
        Box(modifier = Modifier.weight(1f)) {
            DayDetailContent(date = today)
        }
    }
}

@Composable
fun SelectedDateDetailScreen(date: LocalDate, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HomeHeader(date = date, onBack = onBack)
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
        ActionShortcutRow()
        SolarWeekdayBlock(
            weekday = weekday.uppercase(),
            day = date.dayOfMonth
        )
        QuoteCard(quote)

        if (lunar != null && assessment != null) {
            Spacer(modifier = Modifier.height(Spacing10))
            ThreeInfoCards(
                assessment = assessment,
                lunarDay = lunar.day,
                lunarMonth = lunar.month,
                dayCanChi = CanChiCalculator.formatCanChi(dayCanChi),
                monthCanChi = monthCanChi?.let { CanChiCalculator.formatCanChi(it) } ?: "",
                yearCanChi = CanChiCalculator.formatCanChi(yearCanChi)
            )
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            ActivitiesCard(assessment)
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
private fun HomeHeader(date: LocalDate, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack?.invoke() }, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (onBack != null) Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.Menu,
                contentDescription = if (onBack != null) "Trở lại" else "Menu",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(34.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(86.dp)) {
            Text("♛", color = BlocGreen, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
            Text("Lịch Việt", color = BlocGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, BlocGreen)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tháng ${date.monthValue} - ${date.year}",
                    color = BlocGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = BlocGreen)
            }
        }
        Box(
            modifier = Modifier.width(78.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(2.dp, BlocGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${date.dayOfMonth}", color = BlocGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActionShortcutRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShortcutAction(Icons.Outlined.Share, "Chia sẻ", Color(0xFF084C64), Color(0xFFB9C9D0))
        Spacer(modifier = Modifier.width(20.dp))
        ShortcutAction(Icons.Outlined.CameraAlt, "Chụp ảnh", Color(0xFFFF2929), Color(0xFFFFC8C8))
        Spacer(modifier = Modifier.weight(1f))
        ShortcutAction(Icons.Outlined.Event, "Sự kiện", Color(0xFFFF9D00), Color(0xFFE7C37A))
        Spacer(modifier = Modifier.width(20.dp))
        ShortcutAction(Icons.Outlined.EditNote, "Ghi chú", BlocLightGreen, Color(0xFFC5D8BE))
    }
}

@Composable
private fun ShortcutAction(icon: ImageVector, label: String, color: Color, stroke: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(1.dp, stroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(Spacing6))
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SolarWeekdayBlock(weekday: String, day: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(weekday, color = BlocGreen, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "$day",
            color = BlocRed,
            fontSize = 112.sp,
            lineHeight = 112.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-3).sp
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
private fun ThreeInfoCards(
    assessment: DayAssessment,
    lunarDay: Int,
    lunarMonth: Int,
    dayCanChi: String,
    monthCanChi: String,
    yearCanChi: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing8)
    ) {
        HoursPanel(
            title = "GIỜ TỐT",
            hours = assessment.goodHours.take(5),
            color = BlocGreen,
            icon = Icons.Outlined.ThumbUp,
            modifier = Modifier.weight(1f)
        )
        LunarPanel(
            lunarDay = lunarDay,
            lunarMonth = lunarMonth,
            dayCanChi = dayCanChi,
            monthCanChi = monthCanChi,
            yearCanChi = yearCanChi,
            modifier = Modifier.weight(1f)
        )
        HoursPanel(
            title = "GIỜ XẤU",
            hours = assessment.badHours.take(5),
            color = Color(0xFFC91414),
            icon = Icons.Outlined.ThumbDown,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HoursPanel(title: String, hours: List<HourInfo>, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 210.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(Spacing12))
            hours.forEach { hour ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(hour.timeRange.replace("–", " - "), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(hour.chiName, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(46.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun LunarPanel(
    lunarDay: Int,
    lunarMonth: Int,
    dayCanChi: String,
    monthCanChi: String,
    yearCanChi: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 210.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$lunarDay", color = BlocGreen, fontSize = 68.sp, lineHeight = 72.sp, fontWeight = FontWeight.ExtraBold)
            Box(modifier = Modifier.width(72.dp).height(1.dp).background(BlocGreen))
            Text("THÁNG ${lunarMonth.toString().padStart(2, '0')}", color = BlocGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(Spacing12))
            Text("Âm lịch", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Text("Năm $yearCanChi", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, textAlign = TextAlign.Center)
            Text("Tháng $monthCanChi", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, textAlign = TextAlign.Center)
            Text("Ngày $dayCanChi", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, textAlign = TextAlign.Center)
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
                    title = "VIỆC NÊN TRÁNH",
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
