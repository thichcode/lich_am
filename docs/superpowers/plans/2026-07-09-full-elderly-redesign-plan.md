# Full Elderly Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans.

**Goal:** Redesign Android home screen and landing page to match the Tailwind-based elderly HTML prototype.

**Architecture:** Update Theme.kt colors → restructure HomeScreen.kt composables → replace landing/index.html. Core layout: 3-button navigation → calendar block (lunar + can chi + animals) → 2-column good/bad hours with colored headers → clash ages with conflict map → directions.

**Tech Stack:** Kotlin + Jetpack Compose (Android), Tailwind CSS + vanilla JS (landing)

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `app/src/main/java/com/licham/Theme.kt` | Modify | Add elder colors |
| `app/src/main/java/com/licham/HomeScreen.kt` | Modify | Full redesign of DayDetailContent |
| `landing/index.html` | Replace | Full replacement with user's HTML |

---

### Task 1: Update Theme colors + add elder palette

**Files:**
- Modify: `app/src/main/java/com/licham/Theme.kt`

- [ ] **Step 1: Add elder color constants after existing color definitions**

Add after line 11 (`import java.time.LocalTime`):

```kotlin
val ElderGreen = Color(0xFF064E3B)
val ElderRed = Color(0xFF991B1B)
val ElderGold = Color(0xFFB45309)
val ElderBg = Color(0xFFFDFBF7)
val ElderDark = Color(0xFF1F2937)
```

- [ ] **Step 2: Import Color in Theme.kt if not already**

Line 15 already has `import androidx.compose.ui.graphics.Color` — confirm it's there.

- [ ] **Step 3: Build and verify**

Run: `.\gradlew assembleDebug`

---

### Task 2: Redesign HomeScreen.kt — navigation + calendar block

**Files:**
- Modify: `app/src/main/java/com/licham/HomeScreen.kt`

**Key changes:**
- Replace `SolarWeekdayBlock` with `ElderNavigationRow` (3 buttons: Hôm qua | HÔM NAY | Hôm sau)
- Replace existing `LunarDetailCard` + `QuoteCard` content with `CalendarBlockCard`
- Update colors throughout to use `ElderGreen`/`ElderRed`/`ElderGold`

- [ ] **Step 1: Replace `BlocGreen`, `BlocRed`, `BlocLightGreen` with elder colors**

In `HomeScreen.kt`, replace:
```kotlin
private val BlocGreen = Color(0xFF08680E)
private val BlocLightGreen = Color(0xFF4A9D18)
private val BlocRed = Color(0xFFD90000)
```
with:
```kotlin
private val ElderGreen = Color(0xFF064E3B)
private val ElderRed = Color(0xFF991B1B)
private val ElderGold = Color(0xFFB45309)
```

Then do a replaceAll for references:
- `BlocGreen` → `ElderGreen`
- `BlocRed` → `ElderRed`
- `BlocLightGreen` → `ElderGold`

- [ ] **Step 2: Replace `SolarWeekdayBlock` with `ElderNavigationRow`**

Replace the `SolarWeekdayBlock` composable (currently handles weekday/day/month) with a 3-button navigation row:

```kotlin
@Composable
private fun ElderNavigationRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
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
                Text("Hôm qua", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Button(
            onClick = { onDateChange(LocalDate.now()) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElderGold)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HÔM NAY", fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text("Năm ${date.year}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                Text("Hôm sau", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
```

- [ ] **Step 3: Add state management for date in `DayDetailContent`**

`DayDetailContent` currently takes a fixed `date: LocalDate`. Change it to accept a mutable state:

```kotlin
@Composable
fun DayDetailContent(date: LocalDate) {
```

The navigation needs to change the date. Since `SelectedDateDetailScreen` also uses `DayDetailContent`, we need to handle state there too. Add a `var currentDate by remember { mutableStateOf(date) }` inside `DayDetailContent`:

```kotlin
@Composable
fun DayDetailContent(date: LocalDate) {
    var currentDate by remember { mutableStateOf(date) }
    
    // All remembes using currentDate instead of date
    val lunar = remember(currentDate) {
        LunarCalculator.solar2lunar(currentDate.dayOfMonth, currentDate.monthValue, currentDate.year)
    }
    // ... same pattern for all other remember blocks
```

And pass `onDateChange = { currentDate = it }` to `ElderNavigationRow`.

- [ ] **Step 4: Replace the calendar header section**

Remove the old `SolarWeekdayBlock(weekday, day, month, year)` call and replace with `ElderNavigationRow`:

```kotlin
// Remove:
// SolarWeekdayBlock(
//     weekday = weekday.uppercase(),
//     day = date.dayOfMonth,
//     month = date.monthValue,
//     year = date.year
// )

// Add:
ElderNavigationRow(date = currentDate, onDateChange = { currentDate = it })
```

- [ ] **Step 5: Build and verify**

Run: `.\gradlew assembleDebug`

---

### Task 3: Redesign content cards to match HTML

**Files:**
- Modify: `app/src/main/java/com/licham/HomeScreen.kt`

- [ ] **Step 1: Update `QuoteCard` styling**

Use elder colors and thicker border. Replace the existing `QuoteCard`:

```kotlin
@Composable
private fun QuoteCard(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("“", color = Color(0xFFD1D5DB), fontSize = 32.sp, modifier = Modifier.align(Alignment.TopStart))
            Text("”", color = Color(0xFFD1D5DB), fontSize = 32.sp, modifier = Modifier.align(Alignment.TopEnd))
            Text(
                text = quote,
                color = ElderDark,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 2.dp)
            )
        }
    }
}
```

- [ ] **Step 2: Update `LunarDetailCard` with can chi animal names**

Replace the current `LunarDetailCard` with a version that includes animal names and thicker borders:

```kotlin
private val animalNames = arrayOf("Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn")
private val lunarMonthNames = arrayOf("", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp")

@Composable
private fun LunarDetailCard(
    lunarDay: Int,
    lunarMonth: Int,
    dayCanChi: Pair<String, String>,
    monthCanChi: Pair<String, String>?,
    yearCanChi: Pair<String, String>,
    chiIndex: Int
) {
    val dayAnimal = remember(chiIndex) { animalNames[chiIndex] }
    // Compute month and year animal indices
    val monthChiIndex = remember(monthCanChi) {
        monthCanChi?.let { chi.indexOf(it.second) } ?: 0
    }
    val yearChiIndex = remember(yearCanChi) {
        chi.indexOf(yearCanChi.second)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(4.dp, ElderGreen)
    ) {
        Column {
            // Red top bar simulating calendar spine
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(ElderRed))

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                // Solar date header
                
                // Quote - inline in the same card
                
                // Lunar section
                Text("Hôm Nay Là Ngày Âm Lịch", ...)
                Row { moon emoji, lunar day (5xl), month }
                
                // 3-column can chi with animal names
                Row(...) {
                    CanChiAnimalColumn("NĂM", yearCanChi, animal = animalNames[yearChiIndex])
                    CanChiAnimalColumn("THÁNG", monthCanChi, animal = animalNames[...])
                    CanChiAnimalColumn("NGÀY", dayCanChi, animal = dayAnimal)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Update `GoodBadHoursRow` with colored headers**

Replace the current `GoodBadHoursRow` and `GoodBadColumn`:

```kotlin
@Composable
private fun GoodBadHoursRow(assessment: DayAssessment) {
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
private fun GoodBadColumnNew(
    title: String,
    hours: List<HourInfo>,
    headerColor: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, headerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored header
            Box(
                modifier = Modifier.fillMaxWidth().background(headerColor).padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$icon $title", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            // Hours grid (2 columns)
            Column(modifier = Modifier.padding(8.dp)) {
                // Show in 2-column grid
                // Each hour: time (top) + label (bottom)
                hours.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { h ->
                            Column(
                                modifier = Modifier
                                    .background(headerColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, headerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(h.timeRange.replace("–", "-"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElderDark)
                                Text("Giờ ${h.chiName}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = headerColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Update `ClashAndDirectionsCard` with conflict map**

Replace the current composable with one using the conflict map:

```kotlin
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
    val chi = arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi")
    val clashAges = conflictMap[chi[chiIndex]] ?: ""
    val hyThan = when (canIndex) { 0,1 -> "Đông Bắc"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }
    val taiThan = when (canIndex) { 0,1 -> "Đông Nam"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }

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
                Text(clashAges, color = ElderRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
```

- [ ] **Step 5: Update ActivitiesCard with FontAwesome-style icons**

Replace the current `ActivitiesCard`:

```kotlin
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
```

- [ ] **Step 6: Build and verify**

Run: `.\gradlew assembleDebug`

---

### Task 4: Replace landing page

**Files:**
- Replace: `landing/index.html`

- [ ] **Step 1: Replace `landing/index.html` with user's HTML file**

Copy the content from `C:\Users\Lenovo\Downloads\l_ch_m_cho_ng_i_cao_tu_i.html` to `landing/index.html`.

- [ ] **Step 2: Verify landing page loads**

Open in browser and check layout.

---

### Task 5: Final verification

- [ ] **Step 1: Run Android build**

```bash
cd app && .\gradlew assembleDebug
```

- [ ] **Step 2: Commit**

```bash
git add .
git commit -m "feat: full elderly-friendly redesign

- Replace theme colors with elder palette (#064E3B, #991B1B, #B45309)
- Add 3-button navigation row (Hôm qua/HÔM NAY/Hôm sau)
- Redesign calendar block with red spine, lunar+can chi+animals
- Redesign good/bad hours with colored background headers
- Add conflict map for tuổi xung khắc
- Update directions display
- Replace landing page with Tailwind+FontAwesome HTML prototype
- Add elder color constants to Theme.kt"
```
