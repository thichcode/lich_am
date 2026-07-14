# WebView APK Stabilization Design

## Goal

Make the current WebView-based Android APK trustworthy and testable without migrating the UI back to Jetpack Compose. The shipped app must use real deterministic calendar logic, never present fabricated online data, avoid unnecessary WebView privileges, and pass the available build quality gates.

## Scope

### In scope

- Keep `MainActivity` launching the bundled WebView UI.
- Replace parity-based and hard-coded good-day behavior with one deterministic JavaScript assessment engine.
- Use the same assessment output for the day view, month view, and the three good-day filters.
- Correct current-hour boundaries, solar-term year boundaries, and foreground/date refresh behavior.
- Replace random or stale online-data fallbacks with explicit error states and retry actions.
- Render remote RSS fields without HTML injection.
- Remove unnecessary universal file-origin access from the WebView.
- Resolve current Android lint errors that block the quality gate.
- Add automated tests for calendar vectors and active JavaScript behavior.

### Out of scope

- Migrating the WebView UI to Compose.
- Re-enabling the dormant native screens or update installer.
- Treating the current native Kotlin calendar engine as the runtime source of truth.
- Generating or replacing a production signing key.
- Redesigning the visual language of the app.

## Architecture

The bundled JavaScript remains the single runtime source of truth for all data shown by the WebView. Calendar and assessment calculations will be exposed as pure functions with no DOM dependency. Rendering functions consume their results and update the existing interface.

The Android layer remains a small WebView host. It enables only capabilities required by the bundled UI and forwards lifecycle refreshes to JavaScript. It does not introduce a JavaScript-to-native data bridge in this phase.

This avoids maintaining a third data flow between the currently divergent JavaScript and dormant Kotlin implementations. The native implementation remains compiled but is not used by the shipped UI.

## Calendar And Assessment Logic

The existing Ho Ngoc Duc-style solar-to-lunar conversion remains in use because representative vectors already pass:

- 22 January 2023 -> lunar 1/1/2023
- 10 February 2024 -> lunar 1/1/2024
- 29 January 2025 -> lunar 1/1/2025
- 17 February 2026 -> lunar 1/1/2026
- 23 May 2020 -> leap lunar 1/4/2020

The assessment engine will replace `jd % 2` and the fixed July 2026 list. Its inputs are the solar date, lunar date, Julian day, and day Can Chi. Its output includes:

- A deterministic score and label.
- Good and bad activities.
- Hoang Dao and Hac Dao hours.
- Twelve Officers information used by category filtering.
- Stable category suitability for wedding, opening, and groundbreaking searches.

The scoring rules will use explicit data tables in JavaScript for the Twelve Officers, lunar taboo days, and category suitability. The rules are traditional advisory data, not a factual guarantee; labels in the UI remain advisory rather than absolute claims.

The month view and good-day list must call this same engine. The good-day search scans upcoming dates from the selected/current month and returns calculated results for the selected category instead of static samples.

## Time And Lifecycle

Hour branches use these boundaries: Ty is 23:00-00:59, Suu is 01:00-02:59, and subsequent branches advance every two hours.

The Web UI refreshes time-sensitive data:

- At the next hour boundary while visible.
- At the next local midnight while visible.
- When the Android activity returns to the foreground.

Foreground refresh preserves an explicitly selected historical date. It only resets to the new current date when the user was previously viewing the old current date.

Solar-term display must attach the previous year to Dong Chi before Tieu Han and the next year to Tieu Han after Dong Chi. The existing fixed term dates remain approximate; the UI must not label them as exact astronomical timestamps.

## Online Data And Security

News and lottery requests have three states: loading, success, and error. Error state shows a concise message and a retry button. The app never generates random lottery digits or presents fallback headlines as current remote data.

RSS values are treated as untrusted text. Titles, dates, categories, and prize values are inserted through DOM text properties. Static markup may still be created by local templates, but remote strings are never concatenated into `innerHTML` or inline event handlers.

The WebView keeps JavaScript and DOM storage enabled. Universal access from file URLs is disabled. General file access is disabled unless verification shows the bundled asset cannot load without it. No JavaScript interface is added.

## Android Quality Fixes

The current lint blockers will be corrected at their source:

- Use a checked cursor column index and close the download query cursor.
- Register the download receiver with an explicit exported state compatible with target SDK 34.
- Rewrite the suspicious solar-longitude expression so all correction terms are part of the assigned value.

The unreachable update flow is not completed in this phase. Changes are limited to making its compiled code safe enough for lint and future use.

## Testing

### JavaScript tests

Node-based tests execute the same calendar and assessment functions packaged in the APK. Coverage includes:

- Lunar New Year vectors for 2023-2026.
- The 2020 leap fourth month.
- 10 February 2024 day Can Chi equals Giap Thin.
- Hour boundaries at 22:59, 23:00, 00:59, and 01:00.
- Solar-term year rollover before Tieu Han and after Dong Chi.
- Deterministic assessment output.
- Distinct, calculated results for wedding, opening, and groundbreaking filters.
- Online error states contain no fabricated results.
- Remote strings are rendered as text, not executable markup.

### Android verification

- `assembleDebug` must succeed and produce `app-debug.apk`.
- `lintDebug` must report zero errors.
- JavaScript tests must pass.
- Android instrumentation sources must compile.
- Instrumentation and install smoke tests run only when an emulator or device is available.
- Release packaging is verified only when the existing production keystore properties are available; no replacement signing key is generated.

## Success Criteria

- The active APK path contains no parity-based good-day classification or hard-coded good-day samples.
- Loss of network access cannot produce fake lottery results or fake current news.
- Untrusted RSS content cannot inject markup through the app's renderer.
- Time-sensitive labels recover correctly after hour/day changes and activity resume.
- The debug APK builds, lint has no errors, and calendar/assessment regression tests pass.
- Existing visuals and navigation remain functionally unchanged except for honest loading/error/retry states and calculated good-day content.
