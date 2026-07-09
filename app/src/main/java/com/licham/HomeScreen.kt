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
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Calendar

// Colors moved to Theme.kt

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
    var currentDate by remember { mutableStateOf(date) }

    val lunar = remember(currentDate) {
        LunarCalculator.solar2lunar(currentDate.dayOfMonth, currentDate.monthValue, currentDate.year)
    }
    val jd = remember(currentDate) {
        LunarCalculator.jdFromDate(currentDate.dayOfMonth, currentDate.monthValue, currentDate.year)
    }
    val dayCanChi = remember(currentDate) { CanChiCalculator.getDayCanChi(jd) }
    val yearCanChi = remember(lunar) {
        CanChiCalculator.getYearCanChi(lunar?.year ?: currentDate.year)
    }
    val yearCanIndex = remember(lunar) {
        CanChiCalculator.getYearCanIndex(lunar?.year ?: currentDate.year)
    }
    val monthCanChi = remember(lunar, yearCanIndex) {
        if (lunar != null) CanChiCalculator.getMonthCanChi(lunar.month, yearCanIndex) else null
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

    val assessment = remember(currentDate, lunar, dayCanChi) {
        if (lunar != null) {
            GoodBadEngine.assessDay(lunar.day, lunar.month, canIndex, chiIndex, jd, currentDate.monthValue)
        } else null
    }
    val terms = remember(currentDate) { TietKhiCalculator.getCurrentAndNext(currentDate) }
    val events = remember(currentDate, lunar) {
        if (lunar != null) {
            EventProvider.getTodayEvents(currentDate.dayOfMonth, currentDate.monthValue, lunar.day, lunar.month)
        } else emptyList()
    }
    val quote = remember(currentDate) { QuoteProvider.getRandomQuote().text }

    val weekdayNames = remember {
        arrayOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    }
    val weekday = weekdayNames[currentDate.dayOfWeek.value % 7]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing16)
    ) {
        ElderNavigationRow(date = currentDate, onDateChange = { currentDate = it })

        Spacer(modifier = Modifier.height(Spacing10))
        ElderCalendarBlock(
            weekday = weekday.uppercase(),
            day = currentDate.dayOfMonth,
            monthYear = "Tháng ${currentDate.monthValue} ${currentDate.year}",
            quote = quote,
            lunar = lunar,
            dayCanChi = dayCanChi,
            monthCanChi = monthCanChi,
            yearCanChi = yearCanChi,
            chiIndex = chiIndex
        )

        if (lunar != null && assessment != null) {
            Spacer(modifier = Modifier.height(Spacing10))
            ElderGoodBadHours(assessment = assessment)
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            ElderActivitiesCard(assessment = assessment)
        }

        if (assessment != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            ElderClashDirectionsCard(chiIndex = chiIndex, canIndex = canIndex, dayCanChi = dayCanChi)
        }

        if (terms != null) {
            Spacer(modifier = Modifier.height(Spacing12))
            TermSplitCard(terms.first, terms.second)
        }

        if (events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing12))
            MockupEventsCard(currentDate, lunar, events)
        }
    }
}

@Composable
private fun ElderNavigationRow(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = Spacing6),
        horizontalArrangement = Arrangement.spacedBy(Spacing8)
    ) {
        Button(
            onClick = { onDateChange(date.minusDays(1)) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElderGreen)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◀", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Hôm qua", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        Button(
            onClick = { onDateChange(LocalDate.now()) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElderGold)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HÔM NAY", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Năm ${date.year}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
            }
        }
        Button(
            onClick = { onDateChange(date.plusDays(1)) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElderGreen)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▶", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Hôm sau", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

@Composable
private fun ElderCalendarBlock(
    weekday: String,
    day: Int,
    monthYear: String,
    quote: String,
    lunar: LunarDate?,
    dayCanChi: Pair<String, String>,
    monthCanChi: Pair<String, String>?,
    yearCanChi: Pair<String, String>,
    chiIndex: Int
) {
    val lunarMonthNames = remember {
        arrayOf("", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp")
    }
    val animalNames = remember {
        arrayOf("Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn")
    }
    val chi = remember { arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(4.dp, ElderGreen)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(ElderRed))

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(weekday, color = ElderGreen, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("$day", color = ElderRed, fontSize = 72.sp, lineHeight = 72.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text(monthYear, color = ElderGreen, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(Spacing12))
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)).padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("“", color = Color(0xFFD1D5DB), fontSize = 24.sp, modifier = Modifier.align(Alignment.TopStart))
                    Text("”", color = Color(0xFFD1D5DB), fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd))
                    Text(quote, color = ElderDark, fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 2.dp))
                }

                if (lunar != null) {
                    Spacer(Modifier.height(Spacing12))
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val hourChiName = CanChiCalculator.getHourChiName(currentHour)
                    Text("Hiện tại: Giờ $hourChiName", color = Color(0xFF78716C), fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(Spacing8))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("🌙", fontSize = 28.sp)
                        Spacer(Modifier.width(Spacing10))
                        Text("${lunar.day}", color = ElderGreen, fontSize = 48.sp, lineHeight = 48.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(Spacing10))
                        Text("THÁNG ${lunarMonthNames.getOrElse(lunar.month) { lunar.month.toString() }}", color = Color(0xFF1F2937), fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(Modifier.height(Spacing10))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val monthChiIdx = monthCanChi?.let { chi.indexOf(it.second) } ?: 0
                        val yearChiIdx = chi.indexOf(yearCanChi.second)
                        CanChiAnimalColumn("NĂM", "${yearCanChi.first} ${yearCanChi.second}", animalNames.getOrElse(yearChiIdx) { "" })
                        CanChiAnimalColumn("THÁNG", monthCanChi?.let { "${it.first} ${it.second}" } ?: "", animalNames.getOrElse(monthChiIdx) { "" })
                        CanChiAnimalColumn("NGÀY", "${dayCanChi.first} ${dayCanChi.second}", animalNames.getOrElse(chiIndex) { "" })
                    }
                }
            }
        }
    }
}

@Composable
private fun CanChiAnimalColumn(label: String, value: String, animal: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, color = Color(0xFF78716C), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(Spacing4))
        Text(value, color = Color(0xFF1F2937), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("($animal)", color = ElderGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ElderGoodBadHours(assessment: DayAssessment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing8)
    ) {
        GoodBadColumnNew(
            title = "GIỜ HOÀNG ĐẠO",
            hours = assessment.goodHours.take(6),
            headerColor = ElderGreen,
            icon = "✅",
            modifier = Modifier.weight(1f)
        )
        GoodBadColumnNew(
            title = "GIỜ HẮC ĐẠO",
            hours = assessment.badHours.take(6),
            headerColor = ElderRed,
            icon = "❌",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GoodBadColumnNew(title: String, hours: List<HourInfo>, headerColor: Color, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, headerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().background(headerColor).padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$icon $title", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.padding(8.dp)) {
                hours.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { h ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(headerColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                    .border(1.dp, headerColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(h.timeRange.replace("–", "-"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ElderDark)
                                Text("Giờ ${h.chiName}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = headerColor)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ElderClashDirectionsCard(chiIndex: Int, canIndex: Int, dayCanChi: Pair<String, String>) {
    val conflictMap = remember {
        mapOf(
            "Tý" to "Mậu Ngọ, Nhâm Ngọ, Canh Tý",
            "Sửu" to "Kỷ Mùi, Quý Mùi, Tân Sửu",
            "Dần" to "Canh Thân, Giáp Thân, Mậu Dần",
            "Mão" to "Tân Dậu, Ất Dậu, Kỷ Mão",
            "Thìn" to "Nhâm Tuất, Bính Tuất, Giáp Thìn",
            "Tỵ" to "Quý Hợi, Đinh Hợi, Ất Tỵ",
            "Ngọ" to "Nhâm Tý, Bính Tý, Giáp Ngọ",
            "Mùi" to "Quý Sửu, Đinh Sửu, Ất Mùi",
            "Thân" to "Mậu Dần, Bính Dần, Canh Thân",
            "Dậu" to "Kỷ Mão, Đinh Mão, Tân Dậu",
            "Tuất" to "Canh Thìn, Bính Thìn, Mậu Tuất",
            "Hợi" to "Tân Tỵ, Đinh Tỵ, Kỷ Hợi"
        )
    }
    val chi = remember { arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi") }
    val chiName = chi[chiIndex]
    val clashAges = conflictMap[chiName] ?: ""

    val hyThan = remember(canIndex) {
        when (canIndex) { 0,1 -> "Đông Bắc"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }
    }
    val taiThan = remember(canIndex) {
        when (canIndex) { 0,1 -> "Đông Nam"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(3.dp, ElderGold)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(Modifier.weight(1f)) {
                Text("⚡ TUỔI XUNG KHẮC", color = Color(0xFF92400E), fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text(clashAges, color = ElderRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text("🧭 HƯỚNG TỐT", color = ElderGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text("Hỷ Thần: $hyThan", color = ElderGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Tài Thần: $taiThan", color = ElderGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ElderActivitiesCard(assessment: DayAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(3.dp, ElderGreen)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text("🎋", fontSize = 72.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp).alpha(0.2f))
            Column(modifier = Modifier.padding(16.dp)) {
                val gFallback = GoodBadEngine.getTrucGoodActivities(assessment.trucIdx).joinToString(", ")
                val bFallback = GoodBadEngine.getTrucBadActivities(assessment.trucIdx).joinToString(", ")
                ElderActivityLine(
                    icon = "✅",
                    iconColor = ElderGreen,
                    title = "VIỆC NÊN LÀM",
                    text = assessment.goodActivities.joinToString(", ").ifBlank { gFallback }
                )
                Spacer(Modifier.height(16.dp))
                ElderActivityLine(
                    icon = "❌",
                    iconColor = ElderRed,
                    title = "VIỆC TRÁNH (KỴ)",
                    text = assessment.badActivities.joinToString(", ").ifBlank { bFallback }
                )
            }
        }
    }
}

@Composable
private fun ElderActivityLine(icon: String, iconColor: Color, title: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = iconColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            Text(text, color = Color(0xFF1F2937), fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
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
        Icon(Icons.Outlined.Spa, contentDescription = null, tint = ElderGreen, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(Spacing10))
        Column {
            Text(title, color = ElderGreen, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
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
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = ElderGold, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(Spacing16))
            Column(modifier = Modifier.weight(1f)) {
                Text("SỰ KIỆN - NGÀY LỄ", color = ElderGreen, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
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
                        Text(event, color = ElderGreen, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
