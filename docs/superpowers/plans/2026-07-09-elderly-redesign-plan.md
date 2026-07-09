# Elderly-Focused UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure HomeScreen.kt and landing/index.html to match the elderly-optimized HTML reference: dedicated LunarDetailCard, 2-column good/bad hours, updated activities title, new clash+directions section.

**Architecture:** All changes are within `DayDetailContent` composable in HomeScreen.kt. Remove the old 3-panel `ThreeInfoCards` (HoursPanel + LunarPanel + HoursPanel) and replace with: (1) `LunarDetailCard` with moon emoji + large lunar day + 3-column can chi, (2) `GoodBadHoursRow` as 2-column grid, (3) `ActivitiesCard` with renamed title, (4) `ClashAndDirectionsCard`. Landing page mirrors the same structural changes in HTML/CSS.

**Tech Stack:** Kotlin + Jetpack Compose (Android), HTML + CSS + vanilla JS (landing)

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `app/src/main/java/com/licham/HomeScreen.kt` | Modify | Restructure composables in `DayDetailContent` |
| `landing/index.html` | Modify | Mirror Android layout changes |

---

### Task 1: Replace 3-panel row with LunarDetailCard + GoodBadHoursRow

**Files:**
- Modify: `app/src/main/java/com/licham/HomeScreen.kt:136-146`

**Changes:**
- Remove the `ThreeInfoCards` call from `DayDetailContent`
- Remove `ThreeInfoCards`, `HoursPanel`, `LunarPanel` composables
- Add `LunarDetailCard` composable — full-width card with warm gold background (`0xFFFFF8E1`), 2dp border (`0xFFD4A843`), containing:
  - 🌙 + "Âm lịch" label row
  - Large lunar day number (36sp, bold, dark red `0xFF8B0000`)
  - Lunar month name
  - 3-column can chi grid: Giờ | Ngày | Tháng | Năm
- Add `GoodBadHoursRow` composable — 2-column Row with equal weight cards
  - Left: "Giờ Hoàng Đạo" green header, filtered good hours from `assessment.goodHours`
  - Right: "Giờ Hắc Đạo" red header, filtered bad hours from `assessment.badHours`
  - Each shows hour time range + chi name per entry

- [ ] **Step 1: Remove old composables and replace content in DayDetailContent**

Replace the `ThreeInfoCards` block (lines 136-146) and the three old composables with new ones:

```kotlin
// In DayDetailContent, replace:
// Spacer(modifier = Modifier.height(Spacing10))
// ThreeInfoCards(...)

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
```

- [ ] **Step 2: Add the `LunarDetailCard` composable before `ActivitiesCard`**

```kotlin
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
                CanChiLabel("Giờ", dayCanChi.let { (can, chi) ->
                    val hCan = CanChiCalculator.getCanChiHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), canIndex)
                    "${hCan.first} ${hCan.second}"
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
```

- [ ] **Step 3: Add the `GoodBadHoursRow` composable**

```kotlin
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
```

- [ ] **Step 4: Add the missing import for Calendar**

```kotlin
import java.util.Calendar
```

- [ ] **Step 5: Remove the old unused composables** from the file

Delete the entire `ThreeInfoCards` function (lines 226-262), `HoursPanel` function (lines 264-298), and `LunarPanel` function (lines 300-329).

- [ ] **Step 6: Build and verify**

Run: `./gradlew assembleDebug`

---

### Task 2: Update ActivitiesCard and add ClashAndDirectionsCard

**Files:**
- Modify: `app/src/main/java/com/licham/HomeScreen.kt`

- [ ] **Step 1: Rename activity title**

In `ActivitiesCard` (line 352), change:
```kotlin
title = "VIỆC NÊN TRÁNH",
```
to:
```kotlin
title = "VIỆC TRÁNH (KỴ)",
```

- [ ] **Step 2: Add ClashAndDirectionsCard** after `ActivitiesCard` call

In `DayDetailContent`, after the `ActivitiesCard` block, add:

```kotlin
if (assessment != null && lunar != null) {
    Spacer(modifier = Modifier.height(Spacing12))
    ClashAndDirectionsCard(
        chiIndex = chiIndex,
        canIndex = canIndex,
        lunarDay = lunar.day,
        lunarMonth = lunar.month
    )
}
```

- [ ] **Step 3: Implement ClashAndDirectionsCard composable**

```kotlin
@Composable
private fun ClashAndDirectionsCard(chiIndex: Int, canIndex: Int, lunarDay: Int, lunarMonth: Int) {
    val chiNames = remember { arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi") }
    val clashChi = (chiIndex + 6) % 12
    val clashName = chiNames[clashChi]

    val hyThan = remember(canIndex) {
        when (canIndex) {
            0, 1 -> "Đông" // Giáp, Ất
            2, 3 -> "Nam"  // Bính, Đinh
            4, 5 -> "Trung cung" // Mậu, Kỷ
            6, 7 -> "Tây"  // Canh, Tân
            else -> "Bắc"  // Nhâm, Quý
        }
    }
    val taiThan = remember(canIndex) {
        when (canIndex) {
            0, 1 -> "Đông Bắc"
            2, 3 -> "Chính Tây"
            4, 5 -> "Chính Đông"
            6, 7 -> "Chính Nam"
            else -> "Tây Bắc"
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
```

- [ ] **Step 4: Build and verify**

Run: `./gradlew assembleDebug`

---

### Task 3: Update landing page to mirror Android changes

**Files:**
- Modify: `landing/index.html`

- [ ] **Step 1: Replace the 3-panel row with new layout**

Replace lines 513-527 (the `three-panel-row` div):

```html
<div class="lunar-detail-card">
  <div class="ld-header">🌙 Âm Lịch</div>
  <div class="ld-day" id="lunarDayLarge"></div>
  <div class="ld-month" id="lunarMonthLabel"></div>
  <div class="ld-canchi-grid">
    <div class="ld-item"><span class="ld-label">Giờ</span><span class="ld-value" id="hourCanChi"></span></div>
    <div class="ld-item"><span class="ld-label">Ngày</span><span class="ld-value" id="dayCanChiDisplay"></span></div>
    <div class="ld-item"><span class="ld-label">Tháng</span><span class="ld-value" id="monthCanChiDisplay"></span></div>
    <div class="ld-item"><span class="ld-label">Năm</span><span class="ld-value" id="yearCanChiDisplay"></span></div>
  </div>
</div>

<div class="goodbad-row">
  <div class="gb-card" id="goodHoursCard">
    <div class="gb-title" style="color:#2E7D32">GIỜ HOÀNG ĐẠO</div>
    <div id="goodHoursDetail"></div>
  </div>
  <div class="gb-card" id="badHoursCard">
    <div class="gb-title" style="color:#D32F2F">GIỜ HẮC ĐẠO</div>
    <div id="badHoursDetail"></div>
  </div>
</div>
```

- [ ] **Step 2: Add clash + directions card** in the HTML after the activity card section

```html
<div class="clash-dir-card" id="clashDirCard" style="display:none">
  <div class="cd-row">
    <div class="cd-col">
      <div class="cd-header" style="color:#D32F2F">TUỔI XUNG KHẮC</div>
      <div class="cd-value" id="clashValue"></div>
    </div>
    <div class="cd-col">
      <div class="cd-header" style="color:#2E7D32">HƯỚNG TỐT</div>
      <div class="cd-value" id="hyThanValue"></div>
      <div class="cd-value" id="taiThanValue"></div>
    </div>
  </div>
</div>
```

- [ ] **Step 3: Update activity title in JS**

In `renderHome` (line 934-935), change:
```javascript
`<div class="activity-line"><div class="activity-icon" style="color:#C91414">✕</div>...VIỆC NÊN TRÁNH...</div>`
```
to:
```javascript
`<div class="activity-line"><div class="activity-icon" style="color:#C91414">✕</div>...VIỆC TRÁNH (KỴ)...</div>`
```

- [ ] **Step 4: Add CSS for new sections**

Add before the `@media` section:

```css
.lunar-detail-card {
  margin: 0 18px 12px;
  background: #FFF8E1;
  border: 2px solid #D4A843;
  border-radius: 12px;
  padding: 18px 20px;
}
.ld-header { font-size:20px; font-weight:700; color:#8B0000; margin-bottom:6px; }
.ld-day { font-size:40px; font-weight:700; color:#8B0000; line-height:1.1; }
.ld-month { font-size:18px; color:#5F6368; margin-bottom:16px; }
.ld-canchi-grid { display:flex; justify-content:space-between; }
.ld-item { text-align:center; }
.ld-label { display:block; font-size:13px; color:#5F6368; margin-bottom:4px; }
.ld-value { font-size:16px; font-weight:700; color:#202124; }

.goodbad-row {
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:8px;
  padding:0 18px;
  margin-bottom:12px;
}
.gb-card {
  background:var(--surface);
  border:1px solid var(--outline);
  border-radius:10px;
  padding:14px 12px;
}
.gb-title { font-size:16px; font-weight:900; margin-bottom:12px; }
.gb-hour-row { display:flex; justify-content:space-between; padding:2px 0; }
.gb-hour-row .time { font-weight:800; font-size:12px; }
.gb-hour-row .chi { font-size:14px; }

.clash-dir-card {
  margin:12px 18px 0;
  border:1px solid var(--outline);
  border-radius:10px;
  background:var(--surface);
  padding:18px;
}
.clash-dir-card .cd-row { display:flex; justify-content:space-around; }
.clash-dir-card .cd-col { text-align:center; }
.clash-dir-card .cd-header { font-size:14px; font-weight:900; margin-bottom:8px; }
.clash-dir-card .cd-value { font-size:18px; font-weight:700; color:var(--text-primary); }
```

- [ ] **Step 5: Update renderHome JS to populate new sections**

In the `renderHome` function, after the quote rendering block (after `get('quoteText').textContent = q;`), update the lunar section to use the new structure. Also add clash/directions rendering. Replace the entire `if (lunar && mc && yc)` block (lines 918-924):

```javascript
const lc = root.querySelector('#lunarDayLarge');
if (lc && lunar) {
  lc.textContent = `${lunar.day}`;
}
const lml = root.querySelector('#lunarMonthLabel');
if (lml && lunar) {
  const mn = ['','Giêng','Hai','Ba','Tư','Năm','Sáu','Bảy','Tám','Chín','Mười','Một','Chạp'];
  lml.textContent = `Tháng ${mn[lunar.month] || lunar.month}`;
}
```

And replace the `goodHoursList` and `badHoursList` population to use the new selectors:

```javascript
const ghEl = root.querySelector('#goodHoursDetail');
if (ghEl && as) {
  ghEl.innerHTML = as.goodHours.slice(0,6).map(h =>
    `<div class="gb-hour-row"><span class="time" style="color:#2E7D32">${h.time.replace('–',' - ')}</span><span class="chi">${h.chi}</span></div>`
  ).join('');
}
const bhEl = root.querySelector('#badHoursDetail');
if (bhEl && as) {
  bhEl.innerHTML = as.badHours.slice(0,6).map(h =>
    `<div class="gb-hour-row"><span class="time" style="color:#D32F2F">${h.time.replace('–',' - ')}</span><span class="chi">${h.chi}</span></div>`
  ).join('');
}
```

Update the Can Chi display fields:
```javascript
const dcc = root.querySelector('#dayCanChiDisplay');
if (dcc && cc) dcc.textContent = `${cc.can} ${cc.chi}`;
const mcc = root.querySelector('#monthCanChiDisplay');
if (mcc && mc) mcc.textContent = `${mc.can} ${mc.chi}`;
const ycc = root.querySelector('#yearCanChiDisplay');
if (ycc && yc) ycc.textContent = `${yc.can} ${yc.chi}`;
const hcc = root.querySelector('#hourCanChi');
if (hcc && cc) {
  const h = new Date().getHours();
  const hd = ((cc.canIndex * 2 + Math.floor(h / 2)) % 10);
  hcc.textContent = `${CAN[hd]} ${CHI[Math.floor(h / 2) % 12]}`;
}
```

Add clash + directions rendering:
```javascript
const cdCard = root.querySelector('#clashDirCard');
if (cdCard && as && cc) {
  cdCard.style.display = 'block';
  const clashChi = (cc.chiIndex + 6) % 12;
  const clashName = CHI[clashChi];
  root.querySelector('#clashValue').textContent = `Tuổi ${clashName}`;
  
  const hyThan = [0,1].includes(cc.canIndex) ? 'Đông' : [2,3].includes(cc.canIndex) ? 'Nam' : [4,5].includes(cc.canIndex) ? 'Trung cung' : [6,7].includes(cc.canIndex) ? 'Tây' : 'Bắc';
  const taiThan = [0,1].includes(cc.canIndex) ? 'Đông Bắc' : [2,3].includes(cc.canIndex) ? 'Chính Tây' : [4,5].includes(cc.canIndex) ? 'Chính Đông' : [6,7].includes(cc.canIndex) ? 'Chính Nam' : 'Tây Bắc';
  root.querySelector('#hyThanValue').textContent = `Hỷ Thần ${hyThan}`;
  root.querySelector('#taiThanValue').textContent = `Tài Thần ${taiThan}`;
}
```

- [ ] **Step 6: Verify landing page renders correctly**

Run: Open `landing/index.html` in a browser and check layout.

---

### Task 4: Final verification

**Files:**
- Verify: `app/` and `landing/`

- [ ] **Step 1: Run Android build**

Run: `./gradlew assembleDebug`

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/licham/HomeScreen.kt landing/index.html docs/superpowers/plans/2026-07-09-elderly-redesign-plan.md
git commit -m "feat: restructure home screen for elderly-friendly redesign

- Replace 3-panel row with LunarDetailCard + GoodBadHoursRow
- Add ClashAndDirectionsCard (tuổi xung khắc + hướng tốt)
- Rename VIỆC NÊN TRÁNH to VIỆC TRÁNH (KỴ)
- Mirror all changes in landing page"
```
