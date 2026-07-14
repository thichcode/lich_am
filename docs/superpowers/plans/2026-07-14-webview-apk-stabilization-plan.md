# WebView APK Stabilization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the current WebView APK use deterministic calendar and good-day logic, honest and injection-safe online states, restricted WebView permissions, lifecycle refreshes, and passing automated quality gates.

**Architecture:** Move all DOM-independent calendar and assessment behavior into one browser/CommonJS module loaded before `app.js`. Keep `app.js` responsible for state and rendering, while the Android layer remains a small lifecycle-aware WebView host. Test the exact JavaScript shipped in the APK with Node's built-in test runner and retain the existing visual structure.

**Tech Stack:** Kotlin 1.9.22, Jetpack Compose, Android WebView, vanilla JavaScript, Node.js `node:test`, Gradle/Android lint

---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `package.json` | Create | Expose the zero-dependency JavaScript regression-test command. |
| `tests/calendar-core.test.js` | Create | Verify lunar, Can Chi, hour, solar-term, and assessment behavior. |
| `tests/web-ui-source.test.js` | Create | Prevent fabricated online data and unsafe remote-string rendering from returning. |
| `app/src/main/assets/ui/default/assets/calendar-core.js` | Create | Pure calendar, hour, solar-term, and good-day assessment engine for browser and Node. |
| `app/src/main/assets/ui/default/assets/app.js` | Modify | Keep UI state/rendering only; consume `LichAmCore`; render calculated dates and safe online states. |
| `app/src/main/assets/ui/default/index.html` | Modify | Load the core before the UI and correct online/offline disclosure text. |
| `app/src/main/assets/ui/default/assets/app.css` | Modify | Style reusable online loading/error/retry states without changing the existing visual language. |
| `app/src/main/java/com/licham/AppWebViewClient.kt` | Create | Serve packaged assets from a safe HTTPS origin and proxy only an exact RSS allowlist. |
| `app/src/main/java/com/licham/WebViewHomeScreen.kt` | Modify | Restrict WebView privileges and forward resume events to JavaScript. |
| `app/src/main/java/com/licham/SettingsScreen.kt` | Modify | Correct cursor/receiver lifecycle issues reported by lint. |
| `app/src/main/java/com/licham/TietKhiCalculator.kt` | Modify | Make all solar-longitude correction terms part of the assigned expression. |

Do not modify the dormant Kotlin calendar engine in this plan. It is not called by `MainActivity`, and mixing that migration into APK stabilization would restore two runtime sources of truth.

### Task 1: Add The JavaScript Regression Harness

**Files:**
- Create: `package.json`
- Create: `tests/calendar-core.test.js`
- Create: `app/src/main/assets/ui/default/assets/calendar-core.js`
- Modify: `app/src/main/assets/ui/default/index.html:7-8,300`
- Modify: `app/src/main/assets/ui/default/assets/app.js:1-107`

- [ ] **Step 1: Add the test command**

Create `package.json` with no third-party dependencies:

```json
{
  "name": "lich-am",
  "private": true,
  "scripts": {
    "test": "node --test tests/*.test.js"
  }
}
```

- [ ] **Step 2: Write failing calendar-vector tests**

Create `tests/calendar-core.test.js`:

```javascript
const test = require('node:test');
const assert = require('node:assert/strict');
const core = require('../app/src/main/assets/ui/default/assets/calendar-core.js');

test('converts known Tet dates in Vietnam time', () => {
  assert.deepEqual(core.convertSolar2Lunar(22, 1, 2023, 7), [1, 1, 2023, 0]);
  assert.deepEqual(core.convertSolar2Lunar(10, 2, 2024, 7), [1, 1, 2024, 0]);
  assert.deepEqual(core.convertSolar2Lunar(29, 1, 2025, 7), [1, 1, 2025, 0]);
  assert.deepEqual(core.convertSolar2Lunar(17, 2, 2026, 7), [1, 1, 2026, 0]);
});

test('preserves the leap fourth month in 2020', () => {
  assert.deepEqual(core.convertSolar2Lunar(23, 5, 2020, 7), [1, 4, 2020, 1]);
});

test('calculates the known Giap Thin day', () => {
  const jd = core.jdFromDate(10, 2, 2024);
  assert.equal(core.getDayCanChi(jd).text, 'Giáp Thìn');
});
```

- [ ] **Step 3: Run the test and verify the module is missing**

Run: `npm test`

Expected: FAIL with `Cannot find module '../app/src/main/assets/ui/default/assets/calendar-core.js'`.

- [ ] **Step 4: Extract the proven calendar implementation into a browser/CommonJS module**

Create `calendar-core.js` by moving the complete current `app.js:2-95` calculation block (`INT` through `getSolarTerm`) without altering its formulas. Keep every moved declaration between `'use strict';` and the `return` below, then remove the old `getSolarTerm` because Task 2 replaces it. Wrap that exact block with this export boundary:

```javascript
(function (root, factory) {
  var api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  if (root) root.LichAmCore = api;
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  return {
    CANS: CANS,
    CHIS: CHIS,
    ANIMALS: ANIMALS,
    jdFromDate: jdFromDate,
    jdToDate: jdToDate,
    convertSolar2Lunar: convertSolar2Lunar,
    getYearCanChi: getYearCanChi,
    getMonthCanChi: getMonthCanChi,
    getDayCanChi: getDayCanChi,
    getHoursForDay: getHoursForDay
  };
});
```

Delete the moved duplicate definitions from `app.js`. At the top of `app.js`, bind only the values its render code needs:

```javascript
'use strict';
var CANS = LichAmCore.CANS;
var CHIS = LichAmCore.CHIS;
var ANIMALS = LichAmCore.ANIMALS;
var jdFromDate = LichAmCore.jdFromDate;
var convertSolar2Lunar = LichAmCore.convertSolar2Lunar;
var getYearCanChi = LichAmCore.getYearCanChi;
var getMonthCanChi = LichAmCore.getMonthCanChi;
var getDayCanChi = LichAmCore.getDayCanChi;
var getHoursForDay = LichAmCore.getHoursForDay;
```

Load the core first at the end of `index.html`:

```html
<script src="assets/calendar-core.js"></script>
<script src="assets/app.js"></script>
```

- [ ] **Step 5: Run the vectors and build the packaged assets**

Run: `npm test`

Expected: 3 tests PASS.

Run: `.\gradlew.bat assembleDebug`

Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 6: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless the user explicitly requests commits.

### Task 2: Replace Placeholder Good-Day Logic

**Files:**
- Modify: `tests/calendar-core.test.js`
- Modify: `app/src/main/assets/ui/default/assets/calendar-core.js`

- [ ] **Step 1: Add failing hour, term, and deterministic-assessment tests**

Append:

```javascript
test('maps Vietnamese double-hour boundaries correctly', () => {
  assert.equal(core.getHourChiIndex(22), 11);
  assert.equal(core.getHourChiIndex(23), 0);
  assert.equal(core.getHourChiIndex(0), 0);
  assert.equal(core.getHourChiIndex(1), 1);
});

test('assigns solar-term years across New Year', () => {
  const january = core.getSolarTerm(1, 1, 2025);
  assert.equal(january.current, 'Đông Chí');
  assert.equal(january.currentYear, 2024);
  assert.equal(january.next, 'Tiểu Hàn');
  assert.equal(january.nextYear, 2025);

  const december = core.getSolarTerm(25, 12, 2025);
  assert.equal(december.current, 'Đông Chí');
  assert.equal(december.currentYear, 2025);
  assert.equal(december.next, 'Tiểu Hàn');
  assert.equal(december.nextYear, 2026);
});

test('assessment is deterministic and category-aware', () => {
  const date = { day: 10, month: 2, year: 2024 };
  assert.deepEqual(core.assessSolarDate(date), core.assessSolarDate(date));
  assert.notDeepEqual(
    core.findGoodDays(7, 2026, 'cuoihoi', 3),
    core.findGoodDays(7, 2026, 'dongtho', 3)
  );
});

test('good-day results are calculated and sorted', () => {
  const results = core.findGoodDays(7, 2026, 'khaitruong', 3);
  assert.equal(results.length, 3);
  assert.ok(results.every((item) => item.assessment.categorySuitable));
  assert.ok(results[0].jd < results[1].jd && results[1].jd < results[2].jd);
});
```

- [ ] **Step 2: Run the focused tests and verify missing APIs**

Run: `node --test tests/calendar-core.test.js`

Expected: FAIL because `getHourChiIndex`, `getSolarTerm`, `assessSolarDate`, and `findGoodDays` are not exported.

- [ ] **Step 3: Implement exact hour and solar-term boundaries**

Add these pure functions to `calendar-core.js` and export them:

```javascript
function getHourChiIndex(hour) {
  if (!Number.isInteger(hour) || hour < 0 || hour > 23) throw new RangeError('hour must be 0..23');
  return Math.floor(((hour + 1) % 24) / 2);
}

function getSolarTerm(day, month, year) {
  var terms = [
    { n: 'Tiểu Hàn', m: 1, d: 5 }, { n: 'Đại Hàn', m: 1, d: 20 },
    { n: 'Lập Xuân', m: 2, d: 4 }, { n: 'Vũ Thủy', m: 2, d: 19 },
    { n: 'Kinh Trập', m: 3, d: 5 }, { n: 'Xuân Phân', m: 3, d: 20 },
    { n: 'Thanh Minh', m: 4, d: 4 }, { n: 'Cốc Vũ', m: 4, d: 20 },
    { n: 'Lập Hạ', m: 5, d: 5 }, { n: 'Tiểu Mãn', m: 5, d: 21 },
    { n: 'Mang Chủng', m: 6, d: 5 }, { n: 'Hạ Chí', m: 6, d: 21 },
    { n: 'Tiểu Thử', m: 7, d: 7 }, { n: 'Đại Thử', m: 7, d: 22 },
    { n: 'Lập Thu', m: 8, d: 7 }, { n: 'Xử Thử', m: 8, d: 23 },
    { n: 'Bạch Lộ', m: 9, d: 7 }, { n: 'Thu Phân', m: 9, d: 23 },
    { n: 'Hàn Lộ', m: 10, d: 8 }, { n: 'Sương Giáng', m: 10, d: 23 },
    { n: 'Lập Đông', m: 11, d: 7 }, { n: 'Tiểu Tuyết', m: 11, d: 22 },
    { n: 'Đại Tuyết', m: 12, d: 7 }, { n: 'Đông Chí', m: 12, d: 21 }
  ];
  var currentIndex = -1;
  for (var i = 0; i < terms.length; i++) {
    if (month > terms[i].m || (month === terms[i].m && day >= terms[i].d)) currentIndex = i;
  }
  var currentYear = year;
  if (currentIndex === -1) {
    currentIndex = terms.length - 1;
    currentYear = year - 1;
  }
  var nextIndex = (currentIndex + 1) % terms.length;
  var nextYear = nextIndex === 0 ? currentYear + 1 : currentYear;
  return {
    current: terms[currentIndex].n,
    currentStart: String(terms[currentIndex].d).padStart(2, '0') + '/' + String(terms[currentIndex].m).padStart(2, '0'),
    currentYear: currentYear,
    next: terms[nextIndex].n,
    nextStart: String(terms[nextIndex].d).padStart(2, '0') + '/' + String(terms[nextIndex].m).padStart(2, '0'),
    nextYear: nextYear
  };
}
```

- [ ] **Step 4: Implement one explicit assessment engine**

Add constants for `TRUC_NAMES`, good/bad Truc indexes, taboo lunar dates, per-category Truc indexes, and activity text. Derive the solar-month branch at the applicable `Tiet` boundary instead of the first Gregorian day:

```javascript
var TRUC_NAMES = ['Kiến', 'Trừ', 'Mãn', 'Bình', 'Định', 'Chấp', 'Phá', 'Nguy', 'Thành', 'Thu', 'Khai', 'Bế'];
var GOOD_TRUC = [2, 3, 4, 8, 10];
var BAD_TRUC = [1, 6, 7, 11];
var CATEGORY_TRUC = {
  cuoihoi: [2, 3, 4, 8, 10],
  khaitruong: [2, 3, 4, 8, 9, 10],
  dongtho: [0, 2, 3, 4, 8, 10]
};
var TAM_NUONG = [3, 7, 13, 18, 22, 27];
var NGUYET_KY = [5, 14, 23];
var DUONG_CONG_KY = ['13/1', '11/2', '9/3', '7/4', '5/5', '3/6', '1/7', '29/7', '27/8', '25/9', '23/10', '21/11', '19/12'];
var CATEGORY_GOOD_TEXT = {
  cuoihoi: 'Cưới hỏi, lễ hỏi, gặp mặt hai gia đình',
  khaitruong: 'Khai trương, mở hàng, ký kết',
  dongtho: 'Động thổ, sửa nhà, khởi công'
};
var TRUC_GOOD_TEXT = [
  'Khởi sự, nhập học, gặp gỡ', 'Dọn dẹp, giải trừ, chữa bệnh',
  'Cầu tài, giao dịch, ký kết', 'Xuất hành, hòa giải, cưới hỏi',
  'Ổn định, nhập trạch, sửa nhà', 'Lập kế hoạch, thu xếp công việc',
  'Tháo dỡ, giải trừ việc cũ', 'Nghỉ ngơi, giữ gìn sức khỏe',
  'Khai trương, cưới hỏi, nhập trạch', 'Thu hoạch, thu hồi công nợ',
  'Xuất hành, khai trương, bắt đầu việc mới', 'Cúng lễ, hoàn tất việc cũ'
];
var TRUC_BAD_TEXT = [
  'Tránh tranh cãi và phá dỡ', 'Tránh khai trương và cưới hỏi',
  'Tránh kiện tụng và xung đột', 'Tránh việc thiếu chuẩn bị',
  'Tránh thay đổi kế hoạch đột ngột', 'Tránh động thổ và đi xa',
  'Tránh cưới hỏi và nhập trạch', 'Tránh khai trương và động thổ',
  'Tránh kiện tụng và tranh cãi', 'Tránh khởi công và đi xa',
  'Tránh phá dỡ và tranh chấp', 'Tránh khai trương, cưới hỏi và động thổ'
];

function categoryGoodText(category) {
  return CATEGORY_GOOD_TEXT[category] || 'Gặp gỡ, chuẩn bị và làm việc thiện';
}

function generalGoodText(trucIndex) {
  return TRUC_GOOD_TEXT[trucIndex];
}

function generalBadText(trucIndex) {
  return TRUC_BAD_TEXT[trucIndex];
}

function getSolarMonthBranch(day, month) {
  var boundaries = [5, 4, 5, 4, 5, 5, 7, 7, 7, 8, 7, 7];
  var before = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
  var after = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0];
  return day >= boundaries[month - 1] ? after[month - 1] : before[month - 1];
}

function assessSolarDate(date, category) {
  var jd = jdFromDate(date.day, date.month, date.year);
  var lunar = convertSolar2Lunar(date.day, date.month, date.year, 7);
  var dayCanChi = getDayCanChi(jd);
  var dayChiIndex = (jd + 1) % 12;
  var monthBranch = getSolarMonthBranch(date.day, date.month);
  var trucIndex = (dayChiIndex - monthBranch + 12) % 12;
  var score = GOOD_TRUC.indexOf(trucIndex) >= 0 ? 20 : BAD_TRUC.indexOf(trucIndex) >= 0 ? -15 : 0;
  if (TAM_NUONG.indexOf(lunar[0]) >= 0) score -= 30;
  if (NGUYET_KY.indexOf(lunar[0]) >= 0) score -= 25;
  if (DUONG_CONG_KY.indexOf(lunar[0] + '/' + lunar[1]) >= 0) score -= 35;
  if (lunar[0] === 1) score += 8;
  if (lunar[0] === 15) score += 10;
  var allowed = category ? CATEGORY_TRUC[category] || [] : GOOD_TRUC;
  var categorySuitable = allowed.indexOf(trucIndex) >= 0 && score > 0;
  return {
    jd: jd,
    lunar: lunar,
    dayCanChi: dayCanChi,
    truc: TRUC_NAMES[trucIndex],
    trucIndex: trucIndex,
    score: score,
    isGood: score > 0,
    label: score >= 30 ? 'Đại Cát' : score > 0 ? 'Ngày Tốt' : score === 0 ? 'Ngày Bình Thường' : 'Ngày Cần Cân Nhắc',
    category: category || null,
    categorySuitable: categorySuitable,
    shouldDo: categorySuitable ? categoryGoodText(category) : generalGoodText(trucIndex),
    shouldAvoid: generalBadText(trucIndex)
  };
}

function findGoodDays(month, year, category, limit) {
  var result = [];
  var cursor = new Date(year, month - 1, 1);
  var maxDays = 93;
  for (var scanned = 0; scanned < maxDays && result.length < limit; scanned++) {
    var date = { day: cursor.getDate(), month: cursor.getMonth() + 1, year: cursor.getFullYear() };
    var assessment = assessSolarDate(date, category);
    if (assessment.categorySuitable) result.push({ date: date, jd: assessment.jd, assessment: assessment });
    cursor.setDate(cursor.getDate() + 1);
  }
  return result;
}
```

Add these names to the module's returned API so the browser and Node execute the same functions:

```javascript
getHourChiIndex: getHourChiIndex,
getSolarTerm: getSolarTerm,
assessSolarDate: assessSolarDate,
findGoodDays: findGoodDays
```

- [ ] **Step 5: Run all core tests**

Run: `node --test tests/calendar-core.test.js`

Expected: all tests PASS.

- [ ] **Step 6: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless explicitly requested.

### Task 3: Connect The Active UI To The Core

**Files:**
- Create: `tests/web-ui-source.test.js`
- Modify: `app/src/main/assets/ui/default/assets/app.js:89-200,321-353`

- [ ] **Step 1: Add source-level regression tests for removed placeholders**

Create `tests/web-ui-source.test.js`:

```javascript
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const appPath = path.join(__dirname, '../app/src/main/assets/ui/default/assets/app.js');
const source = fs.readFileSync(appPath, 'utf8');

test('active UI contains no parity-based good-day placeholder', () => {
  assert.doesNotMatch(source, /jd\s*%\s*2/);
});

test('active UI contains no fixed July 2026 good-day samples', () => {
  assert.doesNotMatch(source, /15\/07\/2026|22\/07\/2026|27\/07\/2026/);
});
```

- [ ] **Step 2: Run the source tests and verify both fail**

Run: `node --test tests/web-ui-source.test.js`

Expected: 2 FAIL against the current `app.js`.

- [ ] **Step 3: Replace day-view parity logic with `assessSolarDate`**

Inside `getElderDayData`, calculate once:

```javascript
var assessment = LichAmCore.assessSolarDate({ day: day, month: month, year: year });
var term = LichAmCore.getSolarTerm(day, month, year);
```

Use `assessment.isGood`, `assessment.label`, `assessment.shouldDo`, and `assessment.shouldAvoid`. Use `term.currentYear` and `term.nextYear` when formatting dates. Replace the current-hour calculation with:

```javascript
var hourChi = LichAmCore.getHourChiIndex(new Date().getHours());
document.getElementById('current-hour-label').innerText = 'Hiện tại: Giờ ' + CHIS[hourChi];
```

- [ ] **Step 4: Replace hard-coded category results**

In `filterGoodDays(type)`, preserve existing filter-button styling, then call:

```javascript
var results = LichAmCore.findGoodDays(currentMonthView, currentYearView, type, 3);
var list = document.getElementById('good-days-list');
list.innerHTML = '';
results.forEach(function (item) {
  var row = document.createElement('div');
  row.className = 'good-day-item';
  var badge = document.createElement('div');
  badge.className = 'good-day-badge';
  var day = document.createElement('span');
  day.className = 'good-day-num';
  day.textContent = item.date.day;
  var month = document.createElement('span');
  month.className = 'good-day-month';
  month.textContent = 'THÁNG ' + item.date.month;
  badge.append(day, month);
  var info = document.createElement('div');
  info.className = 'good-day-info';
  appendTextElement(info, 'div', 'good-day-desc', item.assessment.dayCanChi.text + ' - ' + item.assessment.label);
  appendTextElement(info, 'div', 'good-day-lunar', 'Âm: ' + item.assessment.lunar[0] + '/' + item.assessment.lunar[1]);
  appendTextElement(info, 'div', 'good-day-sub', item.assessment.shouldDo);
  row.append(badge, info);
  list.appendChild(row);
});
```

Add the reusable helper once:

```javascript
function appendTextElement(parent, tag, className, text) {
  var element = document.createElement(tag);
  element.className = className;
  element.textContent = String(text == null ? '' : text);
  parent.appendChild(element);
  return element;
}
```

- [ ] **Step 5: Run tests and build**

Run: `npm test`

Expected: all core and source tests PASS.

Run: `.\gradlew.bat assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless explicitly requested.

### Task 4: Make Online States Honest And Injection-Safe

**Files:**
- Modify: `tests/web-ui-source.test.js`
- Modify: `app/src/main/assets/ui/default/assets/app.js:203-303`
- Modify: `app/src/main/assets/ui/default/index.html:177-187,198-201,231-239`
- Modify: `app/src/main/assets/ui/default/assets/app.css:114-127,146-160`

- [ ] **Step 1: Add failing anti-fabrication and anti-injection tests**

Append:

```javascript
test('online fallbacks never fabricate lottery or news data', () => {
  assert.doesNotMatch(source, /Math\.random/);
  assert.doesNotMatch(source, /fallbackTitles/);
});

test('remote RSS values are not concatenated into HTML', () => {
  assert.doesNotMatch(source, /news-article-title['"]?>['"]?\s*\+\s*title/);
  assert.doesNotMatch(source, /onclick=['"]showElderAlert\([^)]*msg/);
  assert.match(source, /titleElement\.textContent\s*=\s*article\.title/);
});
```

- [ ] **Step 2: Run the source tests and verify failure**

Run: `node --test tests/web-ui-source.test.js`

Expected: failures identify `Math.random`, `fallbackTitles`, and remote `innerHTML` construction.

- [ ] **Step 3: Add reusable loading/error rendering**

Add to `app.js`:

```javascript
function renderOnlineState(container, state, message, retry) {
  container.innerHTML = '';
  var box = document.createElement('div');
  box.className = 'online-state online-state-' + state;
  appendTextElement(box, 'div', 'online-state-message', message);
  if (retry) {
    var button = document.createElement('button');
    button.type = 'button';
    button.className = 'online-retry';
    button.textContent = 'Thử lại';
    button.addEventListener('click', retry);
    box.appendChild(button);
  }
  container.appendChild(box);
}
```

Add CSS:

```css
.online-state{padding:18px 12px;text-align:center;color:#78716C;font-size:clamp(12px,3.5vw,14px);font-weight:700}
.online-state-error{color:#991B1B}
.online-state-message{line-height:1.5}
.online-retry{margin-top:10px;padding:8px 16px;border:0;border-radius:8px;background:#064E3B;color:#fff;font-weight:900;cursor:pointer}
.online-retry:active{transform:scale(.96)}
```

- [ ] **Step 4: Remove lottery fabrication**

Delete `rn()` and both random catch blocks. On any fetch/parse failure call:

```javascript
renderOnlineState(
  document.getElementById('lottery-body'),
  'error',
  'Không tải được kết quả xổ số. Vui lòng kiểm tra mạng và thử lại.',
  renderLottery
);
```

Build successful lottery rows with `createElement` and `textContent` for region names, labels, values, and dates. Do not pass remote strings through `innerHTML`.

- [ ] **Step 5: Remove fake headlines and render RSS text safely**

Map parsed RSS nodes to plain records:

```javascript
var article = { title: title, category: catName, color: catColor, published: pubDate.substring(0, 16) };
```

Create each article with DOM methods. The title assignment required by the regression test is:

```javascript
var titleElement = document.createElement('div');
titleElement.className = 'news-article-title';
titleElement.textContent = article.title;
```

Attach the click handler with `addEventListener('click', function () { showElderAlert(article.title); })`. On failure, call `renderOnlineState(newsList, 'error', 'Không tải được tin tức. Vui lòng kiểm tra mạng và thử lại.', renderNews)`.

- [ ] **Step 6: Correct product disclosure text**

Change `Ứng dụng ngoại tuyến hoàn toàn` to `Lịch hoạt động ngoại tuyến; tin tức và xổ số cần mạng`. Change the news footer from `Dữ liệu mô phỏng` to `Dữ liệu trực tuyến; có thể gián đoạn khi nguồn không phản hồi`.

- [ ] **Step 7: Run tests and build**

Run: `npm test`

Expected: all tests PASS.

Run: `.\gradlew.bat assembleDebug`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless explicitly requested.

### Task 5: Refresh Time-Sensitive UI And Restrict WebView Access

**Files:**
- Modify: `tests/web-ui-source.test.js`
- Modify: `app/src/main/assets/ui/default/assets/app.js:109-172,321-353`
- Modify: `app/build.gradle.kts:75-97`
- Create: `app/src/main/java/com/licham/AppWebViewClient.kt`
- Modify: `app/src/main/java/com/licham/WebViewHomeScreen.kt:17-47`

- [ ] **Step 1: Add failing lifecycle source tests**

Append:

```javascript
test('UI exposes native resume refresh and avoids universal file access', () => {
  assert.match(source, /window\.onNativeResume\s*=/);
  const webView = fs.readFileSync(
    path.join(__dirname, '../app/src/main/java/com/licham/WebViewHomeScreen.kt'),
    'utf8'
  );
  assert.doesNotMatch(webView, /setAllowUniversalAccessFromFileURLs\(true\)/);
  assert.match(webView, /Lifecycle\.Event\.ON_RESUME/);
  const client = fs.readFileSync(
    path.join(__dirname, '../app/src/main/java/com/licham/AppWebViewClient.kt'),
    'utf8'
  );
  assert.match(client, /WebViewAssetLoader/);
  assert.match(client, /ALLOWED_RSS_URLS/);
});
```

- [ ] **Step 2: Run the source tests and verify failure**

Run: `node --test tests/web-ui-source.test.js`

Expected: FAIL because no resume hook exists and universal file access is enabled.

- [ ] **Step 3: Add hour/day scheduling in JavaScript**

Track the current local date and whether the user is following today:

```javascript
function localDateString(date) {
  return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
}

var lastKnownToday = localDateString(new Date());
var currentDate = lastKnownToday;
var timeRefreshTimer = null;

function refreshForClockChange() {
  var today = localDateString(new Date());
  var followedToday = currentDate === lastKnownToday;
  lastKnownToday = today;
  if (followedToday) currentDate = today;
  if (currentTab === 'home') renderDayView();
  if (currentTab === 'month') renderMonthView();
}

function scheduleClockRefresh() {
  clearTimeout(timeRefreshTimer);
  var now = new Date();
  var nextHour = new Date(now);
  nextHour.setHours(now.getHours() + 1, 0, 1, 0);
  timeRefreshTimer = setTimeout(function () {
    refreshForClockChange();
    scheduleClockRefresh();
  }, Math.max(1000, nextHour.getTime() - now.getTime()));
}

window.onNativeResume = function () {
  refreshForClockChange();
  scheduleClockRefresh();
};
```

Call `scheduleClockRefresh()` from `window.onload`.

- [ ] **Step 4: Serve assets and an exact RSS allowlist from one safe origin**

Add `implementation("androidx.webkit:webkit:1.12.1")` to `app/build.gradle.kts`.

Create `AppWebViewClient.kt`. Use `WebViewAssetLoader` for `/assets/` and intercept only `/rss-proxy?url=...` on `appassets.androidplatform.net`. The allowlist must contain exactly the URLs already used by the UI:

```kotlin
private val ALLOWED_RSS_URLS = setOf(
    "https://vnexpress.net/rss/tin-moi-nhat.rss",
    "https://xskt.com.vn/rss/mien-bac.rss",
    "https://xskt.com.vn/rss/mien-trung.rss",
    "https://xskt.com.vn/rss/mien-nam.rss"
)
```

The client structure is:

```kotlin
class AppWebViewClient(context: Context) : WebViewClientCompat() {
    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        assetLoader.shouldInterceptRequest(request.url)?.let { return it }
        if (request.url.host != "appassets.androidplatform.net" || request.url.path != "/rss-proxy") return null
        val target = request.url.getQueryParameter("url")
            ?: return textResponse(400, "Missing RSS URL")
        if (target !in ALLOWED_RSS_URLS) return textResponse(403, "RSS URL is not allowed")
        return fetchRss(target)
    }
}
```

`fetchRss` must use `HttpsURLConnection` with 10-second connect/read timeouts, reject redirects outside `ALLOWED_RSS_URLS`, cap the buffered response at 2 MiB, disconnect in `finally`, and return either an XML `WebResourceResponse` with the upstream status or a 502 text response. `textResponse` returns UTF-8 `text/plain` from a `ByteArrayInputStream`. These are private methods in `AppWebViewClient`; no URL other than the four constants may reach `HttpsURLConnection`.

In `app.js`, route RSS fetches through the same-origin endpoint:

```javascript
function rssProxyUrl(remoteUrl) {
  return 'https://appassets.androidplatform.net/rss-proxy?url=' + encodeURIComponent(remoteUrl);
}
```

Wrap each of the four existing RSS URLs with `rssProxyUrl(...)`.

- [ ] **Step 5: Remove universal file access and forward lifecycle resume**

In `WebViewHomeScreen.kt`, import lifecycle support:

```kotlin
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
```

Replace `WebViewClient()` with `AppWebViewClient(ctx)`. Load `https://appassets.androidplatform.net/assets/ui/default/index.html`, then disable all file/content access:

```kotlin
settings.allowFileAccess = false
settings.allowContentAccess = false
@Suppress("DEPRECATION")
settings.setAllowUniversalAccessFromFileURLs(false)
@Suppress("DEPRECATION")
settings.setAllowFileAccessFromFileURLs(false)
```

Observe resume after the `AndroidView`:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current

DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            wv?.evaluateJavascript("window.onNativeResume && window.onNativeResume()", null)
            wv?.onResume()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            wv?.onPause()
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}
```

Keep the existing disposal block that destroys the WebView. Do not add a JavaScript interface.

- [ ] **Step 6: Run tests and compile Android**

Run: `npm test`

Expected: all tests PASS.

Run: `.\gradlew.bat assembleDebug compileDebugAndroidTestKotlin`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless explicitly requested.

### Task 6: Clear Android Lint Blockers

**Files:**
- Modify: `app/src/main/java/com/licham/SettingsScreen.kt:408-447`
- Modify: `app/src/main/java/com/licham/TietKhiCalculator.kt:39-46`

- [ ] **Step 1: Reproduce and preserve the lint evidence**

Run: `.\gradlew.bat lintDebug`

Expected before changes: FAIL with `Range`, `UnspecifiedRegisterReceiverFlag`, and `SuspiciousIndentation` errors.

- [ ] **Step 2: Correct the solar-longitude expression grouping**

Replace the ambiguous expression with explicit parentheses:

```kotlin
val correction = (
    (1.914602 - 0.004817 * T - 0.000014 * T * T) * sin(M) +
        (0.019993 - 0.000101 * T) * sin(2.0 * M) +
        0.000289 * sin(3.0 * M)
)
val lambda = L0 + correction
```

- [ ] **Step 3: Correct the receiver and cursor lifecycle**

Import `androidx.core.content.ContextCompat`. Store the receiver in a local variable, register it explicitly as exported because `ACTION_DOWNLOAD_COMPLETE` originates from the system download manager, and close the cursor:

```kotlin
val receiver = object : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (id != downloadId) return
        ctx.unregisterReceiver(this)
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    onError()
                    return
                }
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status != DownloadManager.STATUS_SUCCESSFUL) onError()
            }
        } catch (_: Exception) {
            onError()
        }
    }
}
ContextCompat.registerReceiver(
    context,
    receiver,
    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
    ContextCompat.RECEIVER_EXPORTED
)
```

Do not implement APK installation in this task; the settings screen is unreachable and installation remains explicitly out of scope.

- [ ] **Step 4: Run lint and compilation**

Run: `.\gradlew.bat lintDebug compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`, zero lint errors. Dependency/version and unused-resource warnings may remain.

- [ ] **Step 5: Record the task checkpoint**

Run: `git diff --check`

Expected: no whitespace errors. Do not commit unless explicitly requested.

### Task 7: Full Verification And APK Handoff

**Files:**
- Modify only if verification exposes a defect in an in-scope file.

- [ ] **Step 1: Run the complete JavaScript suite**

Run: `npm test`

Expected: all tests PASS with zero failures.

- [ ] **Step 2: Run a clean Android quality build**

Run: `.\gradlew.bat clean assembleDebug lintDebug testDebugUnitTest compileDebugAndroidTestKotlin`

Expected: `BUILD SUCCESSFUL`. `testDebugUnitTest` may report `NO-SOURCE` because active domain tests run under Node; this is acceptable only when `npm test` passed in Step 1.

- [ ] **Step 3: Verify the APK artifact metadata**

Read `app/build/outputs/apk/debug/output-metadata.json` and confirm:

```text
applicationId = com.vietnamese.lunarcalendar
variantName = debug
outputFile = app-debug.apk
```

- [ ] **Step 4: Check for a connected Android target**

Run: `& "C:\Users\Lenovo\Android\Sdk\platform-tools\adb.exe" devices -l`

Expected without a target: an empty device list; report that install/instrumentation smoke tests were not run.

If an authorized target is listed, run:

```powershell
& "C:\Users\Lenovo\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
.\gradlew.bat connectedDebugAndroidTest
```

Expected: APK install succeeds. Do not claim instrumentation success if the existing WebView-oriented screenshot test fails; report the exact failing assertion because replacing that screenshot workflow is outside this stabilization plan.

- [ ] **Step 5: Confirm release-signing constraints without replacing keys**

If `keystore.properties` exists, run `.\gradlew.bat assembleRelease` and expect `BUILD SUCCESSFUL`. If it does not exist, do not create a key; report that signed release packaging remains dependent on the configured CI secrets/production keystore.

- [ ] **Step 6: Review the final worktree**

Run: `git status --short`

Run: `git diff --check`

Run: `git diff --stat`

Expected: only the spec, plan, test harness, Web UI, and targeted Android files are changed; no whitespace errors or generated build artifacts are tracked.

- [ ] **Step 7: Prepare the completion report**

Report:

- Exact test counts from `npm test`.
- Android build/lint command and exit status.
- APK path.
- Whether a device smoke test ran.
- Whether release signing was available.
- Any remaining warnings or out-of-scope native-engine risks.

Do not commit, push, or create a release unless the user explicitly requests it.
