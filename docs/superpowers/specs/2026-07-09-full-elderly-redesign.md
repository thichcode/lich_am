# Full Elderly-Focused App Redesign

## Goal
Redesign both the Android app (HomeScreen.kt) and landing page (index.html) to match the elderly-optimized HTML prototype (`l_ch_m_cho_ng_i_cao_tu_i.html`). The HTML uses Tailwind CSS, FontAwesome, Hồ Ngọc Đức lunar algorithm, and a completely redesigned UI for elderly users (large text, bold borders, high contrast, sound effects).

## Source Reference
- `C:\Users\Lenovo\Downloads\l_ch_m_cho_ng_i_cao_tu_i.html` (1424 lines)
- Key Taiwind classes: `elder-green: #064E3B`, `elder-red: #991B1B`, `elder-gold: #B45309`, `elder-bg: #FDFBF7`
- 3px borders (`elder-border`), shadow-xl, rounded-2xl
- 5 tabs: Xem ngày (Hôm nay) | Lịch tháng | Xem ngày đẹp | Văn khấn | Tiện ích
- JS: Hồ Ngọc Đức lunar, Gemini API news, auto-scroll prayer reader, bell sound, toast, high-contrast toggle

## Key Design Changes

### Android: HomeScreen.kt

#### 1. Color Palette (Theme.kt)
Add elder-friendly colors and make them the default:
```kotlin
val ElderGreen = Color(0xFF064E3B)
val ElderRed = Color(0xFF991B1B)
val ElderGold = Color(0xFFB45309)
val ElderBg = Color(0xFFFDFBF7)
val ElderDark = Color(0xFF1F2937)
```
Replace `BlocGreen`/`BlocRed` usage with these.

#### 2. Navigation Row (was SolarWeekdayBlock)
Replace current 3-column (weekday | day | month year) with a 3-button grid:
- **Hôm qua** (◀) - green button, navigate -1 day
- **HÔM NAY** (gold button) - reset to today
- **Hôm sau** (▶) - green button, navigate +1 day
This requires adding state management for the currently viewed date.

#### 3. Calendar Block Card
White card (`Card` with `containerColor = White`, 4dp border `ElderGreen`, `RoundedCornerShape(16.dp)`):
- **4dp red top bar** simulating calendar spine
- 3-column grid: weekday (left) | solar day (center, text-8xl, elder-red) | month year (right)
- Quote box: within the same card, with opening/closing quote marks, italic text
- **Lunar section**: centered, with moon emoji, large lunar day (5xl equivalent ~48sp), month name
- 3-column can chi grid: Năm | Tháng | Ngày, each with animal name in parentheses

#### 4. Good/Bad Hours (2-column)
Replace current `GoodBadHoursRow` with colored header chips:
- Left: **GIỜ HOÀNG ĐẠO** - green background header (`ElderGreen`), ✅ icon
- Right: **GIỜ HẮC ĐẠO** - red background header (`ElderRed`), ❌ icon
- Hours displayed as 2-row grid inside each column: time range (top) + giờ name (bottom)
- Each hour is a small rounded chip

#### 5. Activities Card
Keep current structure but:
- Replace Material icons with text-based emoji/Unicode: ✓ → ✅ (or FontAwesome-style check), ✕ → ❌
- Add bamboo SVG decoration (or use 🎋 text as current)
- Keep "VIỆC NÊN LÀM" / "VIỆC TRÁNH (KỴ)" titles

#### 6. Clash Ages + Directions
Replace current `ClashAndDirectionsCard`:
- Add amber border (`ElderGold`, 3dp)
- Left: "⚡ TUỔI XUNG KHẮC" with specific age combos from conflict map
  - Map: day's animal → specific ages (e.g., Khỉ → "Mậu Dần, Bính Dần, Canh Thần")
- Right: "🧭 HƯỚNG TỐT" with Hỷ Thần + Tài Thần directions

The conflict map from the HTML:
```kotlin
private val conflictMap = mapOf(
    "Tý" to "Mậu Ngọ, Nhâm Ngọ, Canh Tý",
    "Sửu" to "Kỷ Mùi, Quý Mùi, Tân Sửu",
    // ... etc
)
```

#### 7. Solar Terms
Same as current `TermSplitCard` but with added leaf emoji and amber styling.

#### 8. News Section (Dynamic Info Box)
Add a section at the bottom that shows either:
- Gemini API news (online) - uses the same fetch approach as the HTML
- Offline message if no connection

#### 9. Sound Effect
Add `MediaPlayer` for bell sound on navigation. Add a toggle in settings.

#### 10. Toast Notifications
Use Android `Snackbar` instead of the custom toast in HTML.

#### 11. High Contrast Mode
Already have dark theme support in `LichAmTheme`. Add a quick toggle in the main screen.

#### 12. Prayer Reader Modal (New Screen)
Create a new `PrayerReaderScreen` composable with:
- Large text for elderly (20sp base, adjustable)
- Auto-scroll toggle (LaunchedEffect with animateScrollTo)
- Search functionality
- Back button to return to prayer list

### Landing: index.html
Replace the entire file with the user's HTML. Already self-contained with Tailwind CDN, FontAwesome, and complete JS.

### Tab Structure
Rename tabs to match HTML:
- Hôm nay → (unchanged)
- Lịch tháng → (unchanged)
- Ngày tốt → "Xem ngày đẹp"
- Văn khấn → (unchanged)
- Thêm → "Tiện ích"

## Files Changed

### Android
- `app/src/main/java/com/licham/Theme.kt` — add elder colors
- `app/src/main/java/com/licham/HomeScreen.kt` — full DayDetailContent redesign
- `app/src/main/java/com/licham/PrayerReader.kt` — new file for prayer reader modal

### Landing
- `landing/index.html` — full replacement

## Out of Scope
- Gemini API key management (uses user-provided key)
- Push notifications
- Offline data caching
