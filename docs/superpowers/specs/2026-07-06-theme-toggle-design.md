# Theme Toggle — Giao diện Sáng/Tối/Theo Hệ Thống/Theo Mặt Trời

Version: 1.0

## Overview

Thêm bộ chọn giao diện với 4 chế độ: Sáng (Light), Tối (Dark), Theo hệ thống (System), Theo mặt trời (Sunrise/Sunset). Áp dụng cho cả Android app và landing page Vercel.

## Android

### ThemeMode enum (Theme.kt)

```kotlin
enum class ThemeMode { LIGHT, DARK, SYSTEM, SUNRISE_SUNSET }
```

### Sunrise/sunset calculation

- Dùng Hanoi coordinates: 21.0285° N, 105.8542° E
- Thuật toán: solar zenith angle → sunrise/sunset giờ địa phương
- Function: `fun isDarkAt(localTime: LocalTime, now: LocalDate = LocalDate.now()): Boolean`
- Không cần library — công thức lượng giác cơ bản (NOOA approximation đơn giản)

### Theme state management

- `LocalThemeMode = staticCompositionLocalOf<MutableState<ThemeMode>>`
- SharedPreferences key `"theme_mode"` lưu ordinal (0-3)
- `MainActivity` trong `onCreate`:
  - load từ SharedPreferences → `mutableStateOf`
  - Compose `LichAmTheme` wrapping
- Timer `LaunchedEffect` 60 giây khi mode = SUNRISE_SUNSET để tự động cập nhật khi trời tối/sáng

### Effective dark resolution

```
effectiveDark = when (mode) {
    LIGHT -> false
    DARK -> true
    SYSTEM -> isSystemInDarkTheme()
    SUNRISE_SUNSET -> isDarkAt(LocalTime.now())
}
```

### Theme.kt changes

- `LichAmTheme(darkTheme: Boolean)` — thêm parameter, bỏ `isSystemInDarkTheme()` bên trong
- Giữ nguyên `LightColorScheme`, `DarkColorScheme` như hiện tại
- `DarkColorScheme` đã work — không cần chỉnh
- Giữ `SeniorTypography`, `SeniorShapes` không đổi

### HomeScreen.kt color substitutions

- Column modifier `Color.White` → `MaterialTheme.colorScheme.background`
- `Color.Black` (text/icon) → `MaterialTheme.colorScheme.onBackground` hoặc `onSurface`
- `BlocSoftCard = Color(0xFFFEFEFE)` → `MaterialTheme.colorScheme.surface` (hoặc `0xFFFEFEFE` surface)
- `BlocCardBorder = Color(0xFFE0E0E0)` → `MaterialTheme.colorScheme.outline`
- `Color.White` trong card backgrounds → `MaterialTheme.colorScheme.surface`
- Giữ nguyên brand colors: `BlocGreen(0xFF08680E)`, `BlocRed(0xFFD90000)`, `BlocLightGreen(0xFF4A9D18)` — mockup identity không đổi theo theme
- `Color(0xFFD4D4D4)` cho quote marks → `MaterialTheme.colorScheme.onSurfaceVariant` (0.38 alpha)
- `Color(0xFFC91414)` bad text → `MaterialTheme.colorScheme.error`

### MainActivity.kt color substitutions

- `Color(0xFF08730C)` selected tab → giữ nguyên (green tab highlight, brand identity)
- `Color.White` selected tab text → giữ nguyên
- `Color(0xFF424242)` unselected icon → `MaterialTheme.colorScheme.onSurfaceVariant`
- `Color(0xFF202124)` unselected text → `MaterialTheme.colorScheme.onSurface`

### SettingsScreen changes

Thêm section mới **Giao diện** sau phần "Thông tin" header:

- Icon: palette/contrast (dùng `Icons.Outlined.DarkMode` hoặc `Icons.Outlined.Contrast`)
- Title: "Giao diện"
- 4 radio buttons: Sáng, Tối, Theo hệ thống, Theo mặt trời
- Separator HorizontalDivider sau cùng

Dùng `RadioButton` + `Row` pattern — mỗi item click gọi `themeModeState.value = mode` + lưu SharedPreferences.

### No new dependencies

SharedPreferences (Android SDK) — không Room, DataStore, Hilt.

## Landing Page

### CSS variables

```css
:root {
  --bg: #FFFFFF;
  --bg-card: #FAFAFA;
  --text-primary: #202124;
  --text-secondary: #5F6368;
  --divider: #EAEAEA;
  --scrollbar-thumb: #EAEAEA;
  --bottom-nav-bg: #FFFFFF;
  --bottom-nav-border: #EAEAEA;
  --hover-bg: #FAFAFA;
  --hover-active: #EAEAEA;
  /* brand colors (unchanged) */
  --green: #08680E;
  --red: #D90000;
  --light-green: #4A9D18;
}

[data-theme="dark"] {
  --bg: #121212;
  --bg-card: #1E1E1E;
  --text-primary: #E0E0E0;
  --text-secondary: #9E9E9E;
  --divider: #424242;
  --scrollbar-thumb: #424242;
  --bottom-nav-bg: #121212;
  --bottom-nav-border: #333;
  --hover-bg: #2D2D2D;
  --hover-active: #424242;
}
```

### Elements to convert

- `body` → `background: var(--bg); color: var(--text-primary)`
- `.top-bar` → `background: var(--bg); border-color: var(--divider)`
- `.scrollbar-thumb` → `background: var(--scrollbar-thumb)`
- `.bottom-nav` → `background: var(--bottom-nav-bg); border-color: var(--bottom-nav-border)`
- `.nav-item:hover` → `background: var(--hover-bg)`
- `.nav-item.active` background → giữ gradient green (brand identity)
- `.card` → `background: var(--bg-card)`
- `.good-item.good/neutral/bad` → giữ nguyên (semantic colors)
- Các `color` references (#111, #202124, #5F6368) → `var(--text-primary)`, `var(--text-secondary)`
- `.cal-cell:hover` → `background: var(--hover-bg)`
- `.cal-cell.today` → giữ `#FFEBEE` (semantic today highlight)

### JS ThemeManager

```javascript
const ThemeManager = {
  mode: 'system', // 'light' | 'dark' | 'system' | 'sunrise'
  init() {
    this.mode = localStorage.getItem('themeMode') || 'system';
    this.apply();
    // sunrise timer: check every 60s
    if (this.mode === 'sunrise') setInterval(() => this.apply(), 60000);
  },
  setMode(m) {
    this.mode = m;
    localStorage.setItem('themeMode', m);
    this.apply();
  },
  isDark() {
    if (this.mode === 'light') return false;
    if (this.mode === 'dark') return true;
    if (this.mode === 'system') return window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (this.mode === 'sunrise') return this.isDarkNow();
    return false;
  },
  isDarkNow() {
    // sunrise ~6h, sunset ~18h Hanoi approximation
    const h = new Date().getHours();
    return h < 5 || h >= 18; // dark: 18h-5h, light: 5h-18h
  },
  apply() {
    if (this.isDark()) {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }
};
```

### Theme UI in tab Thêm

Thêm section "Giao diện" phía trên download button:

```html
<div class="theme-section">
  <div class="theme-title">Giao diện</div>
  <div class="theme-options">
    <button data-mode="light">Sáng</button>
    <button data-mode="dark">Tối</button>
    <button data-mode="system">Hệ thống</button>
    <button data-mode="sunrise">Mặt trời</button>
  </div>
</div>
```

CSS: flex row, mỗi button là card nhỏ, active state border green.

## No Changes

- `CalendarMonthScreen.kt` — đã dùng `MaterialTheme.colorScheme.*` — không cần sửa
- `GoodDayScreen.kt` — đã dùng theme colors — không cần sửa
- `VanKhanScreen.kt` — đã dùng theme colors — không cần sửa
- Block mockup brand colors (`BlocGreen`, `BlocRed`, `BlocLightGreen`, `Color(0xFF084C64)`, `Color(0xFFFF2929)`, `Color(0xFFFF9D00)`) — không đổi
- `QuoteProvider.kt`, `GoodBadEngine.kt`, `EventProvider.kt` — không liên quan
- `DesIGN.md` — không cần update (dark mode mới không thay đổi design spec)
- `scripts/check-home-mockup.js` — không cần sửa

## Files to modify

1. `app/src/main/java/com/licham/Theme.kt`
2. `app/src/main/java/com/licham/MainActivity.kt`
3. `app/src/main/java/com/licham/SettingsScreen.kt`
4. `app/src/main/java/com/licham/HomeScreen.kt`
5. `landing/index.html`
