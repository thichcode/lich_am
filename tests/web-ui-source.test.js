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

test('online fallbacks never fabricate lottery or news data', () => {
  assert.doesNotMatch(source, /Math\.random/);
  assert.doesNotMatch(source, /fallbackTitles/);
});

test('remote RSS values are not concatenated into HTML', () => {
  assert.doesNotMatch(source, /news-article-title['"]?>['"]?\s*\+\s*title/);
  assert.doesNotMatch(source, /onclick=['"]showElderAlert\([^)]*msg/);
  assert.match(source, /titleElement\.textContent\s*=\s*article\.title/);
});

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

test('lottery uses the real RSS feeds and preserves the selected filter', () => {
  assert.match(source, /rss-feed\/mien-bac-xsmb\.rss/);
  assert.match(source, /rss-feed\/mien-trung-xsmt\.rss/);
  assert.match(source, /rss-feed\/mien-nam-xsmn\.rss/);
  assert.match(source, /currentGoodDayCategory/);
  assert.match(source, /item\.assessment\.categoryLabel/);
  assert.match(source, /currentTab==='good-days'[\s\S]*renderLottery\(\)/);
  assert.doesNotMatch(source, /new Date\(dateStr\)/);
});
