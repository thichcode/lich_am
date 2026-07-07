# Calendar Day Home Detail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clicking any day in the month calendar opens a full Home-style detail page for that selected date.

**Architecture:** Android already has reusable `DayDetailContent(date)` and a Home header; expose the header date/back behavior and use it from calendar detail. Landing already has `renderHome(date)`; make calendar cells clickable, render a detail view inside the Calendar tab, and reuse the Home renderer against the selected date.

**Tech Stack:** Kotlin Jetpack Compose, vanilla HTML/CSS/JavaScript, Node structural checks, Gradle Android build.

---

### Task 1: Regression Checks

**Files:**
- Modify: `scripts/check-home-mockup.js`

- [ ] Add checks that Android calendar detail calls `DayDetailContent(date = date)` with a Home header/back path.
- [ ] Add checks that landing calendar cells call `showCalendarDayDetail(...)` and that a `calendarDetailView` exists.
- [ ] Run `node scripts/check-home-mockup.js` and verify it fails before implementation.

### Task 2: Android Calendar Detail

**Files:**
- Modify: `app/src/main/java/com/licham/HomeScreen.kt`
- Modify: `app/src/main/java/com/licham/CalendarMonthScreen.kt`

- [ ] Change `HomeHeader` to accept optional `onBack` and display back arrow when provided.
- [ ] Add a public `SelectedDateDetailScreen(date, onBack)` composable that renders `HomeHeader(date, onBack)` plus `DayDetailContent(date)`.
- [ ] Update `CalendarDayDetail` to call `SelectedDateDetailScreen(date, onBack)` and remove the old compact detail title bar.

### Task 3: Landing Calendar Detail

**Files:**
- Modify: `landing/index.html`

- [ ] Add a hidden `calendarDetailView` inside `tabCalendar` with a back button and a Home-style detail container.
- [ ] Add `showCalendarDayDetail(d, m, y)` that hides the calendar grid, shows the detail view, and calls `renderHome(new Date(y, m - 1, d))`.
- [ ] Add `showCalendarGrid()` to restore the calendar month view and rerender today's Home after returning, so the Home tab remains today's dashboard.
- [ ] Add click handlers to generated `cal-cell` elements.

### Task 4: Verification

**Files:**
- Test: `scripts/check-home-mockup.js`

- [ ] Run `node scripts/check-home-mockup.js` and expect pass.
- [ ] Run landing JS syntax check and expect pass.
- [ ] Run `./gradlew assembleDebug assembleDebugAndroidTest` and expect `BUILD SUCCESSFUL`.
- [ ] Capture a Chrome screenshot if needed to verify the landing detail page visually.
