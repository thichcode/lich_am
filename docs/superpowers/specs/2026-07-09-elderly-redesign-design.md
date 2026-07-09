# Elderly-Focused UI Redesign

## Goal
Redesign the Android home screen and landing page to match user-provided elderly-optimized HTML reference (Tailwind CSS, ultra-large text, high-contrast borders, moon/lunar/giờ layout).

## Source Reference
- File: `l_ch_m_cho_ng_i_cao_tu_i.html` (desktop HTML prototype with Tailwind + FontAwesome + Hồ Ngọc Đức lunar JS)
- Key features: 3-column hero, large lunar box, 2-column good/bad hours, nên làm/tránh (kỵ) activities, xung khắc + hướng tốt section, tiết khí section, high-contrast night mode

## Changes

### HomeScreen.kt (DayDetailContent)

1. **Remove** `ThreeInfoCards` (the 3-panel Row with HoursPanel, LunarPanel, HoursPanel)
2. **Add** `LunarDetailCard` — full-width card with:
   - Moon emoji + "Âm lịch" label
   - Large lunar day number (28+sp)
   - Lunar month name (e.g., "tháng Năm")
   - 3-column Can Chi grid: Giờ, Ngày, Tháng (Năm)
3. **Add** `GoodBadHoursRow` — 2-column Row:
   - Left: "Giờ Hoàng Đạo" (green header) — filtered tốt hours
   - Right: "Giờ Hắc Đạo" (red header) — filtered xấu hours
   - Each column is a card with hour chips
4. **Modify** `ActivitiesCard`:
   - Change "VIỆC NÊN TRÁNH" header to "VIỆC TRÁNH (KỴ)"
   - Keep existing check/cancel icons and activity layout
5. **Add** `ClashAndDirectionsCard`:
   - Row 1: "Tuổi Xung Khắc" — computed from day's chi (Dần/Thân, Mão/Dậu, etc.)
   - Row 2: "Hướng Tốt" — Hỷ Thần + Tài Thần directions (can hardcode as 2-3 common ones)
6. Keep existing: `SolarWeekdayBlock`, `QuoteCard`, `TermSplitCard`, `MockupEventsCard`, `NavigationBar`

### Hồ Ngọc Đức algorithm
- Already used in `LunarCalculator.kt` — no changes needed.

### landing/index.html
- Mirror the same structural changes as Android
- Add 2-column good/bad hours (not in current landing)
- Add clash + directions section (not in current landing)

### Text/Font/Spacing
- Increase body text to 14-15sp (from 12-13sp)
- Increase header sizes by 1-2sp
- Card borders: 2dp (from 1dp) with bold outline
- Minimum touch target: 48dp

## Files Changed
- `app/src/main/java/com/licham/HomeScreen.kt` — `DayDetailContent` restructuring
- `landing/index.html` — mirror Android changes
- (No new files)

## Out of Scope
- Bell sound (OS feature, not in scope)
- News/Gemini API section (user asked for layout match only)
- Auto-scroll prayer reader (complex, not requested for Android)
