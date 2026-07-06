const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const home = fs.readFileSync(path.join(root, 'app/src/main/java/com/licham/HomeScreen.kt'), 'utf8');
const landing = fs.readFileSync(path.join(root, 'landing/index.html'), 'utf8');

const checks = [
  [home, 'HomeHeader(', 'Android Home has mockup header'],
  [home, 'ActionShortcutRow(', 'Android Home has action shortcut row'],
  [home, 'SolarWeekdayBlock(', 'Android Home has weekday/day hero'],
  [home, 'TermSplitCard(', 'Android Home has split solar-term card'],
  [home, 'MockupEventsCard(', 'Android Home has styled event card'],
  [landing, 'class="mock-header"', 'Landing Home has mockup header'],
  [landing, 'class="shortcut-row"', 'Landing Home has action shortcut row'],
  [landing, 'class="term-split-card"', 'Landing Home has split solar-term card'],
  [landing, 'class="events-card"', 'Landing Home has styled event card'],
];

const missing = checks.filter(([text, needle]) => !text.includes(needle));
const duplicates = [
  [home, 'lunarWeekday = weekday.uppercase()', 'Android Home repeats weekday below the solar day'],
  [home, 'lunarWeekday:', 'Android Home still accepts a lower weekday parameter'],
  [landing, 'id="lunarWeekday"', 'Landing Home repeats weekday below the solar day'],
  [landing, "getElementById('lunarWeekday')", 'Landing renderer still writes lower duplicate weekday'],
  [landing, 'class="status-bar"', 'Landing Home still renders a fake phone status bar'],
  [landing, 'class="status-icons"', 'Landing Home still renders fake phone status icons'],
].filter(([text, needle]) => text.includes(needle));

if (missing.length > 0 || duplicates.length > 0) {
  console.error('Home mockup checks failed:');
  for (const [, , label] of missing) console.error(`- ${label}`);
  for (const [, , label] of duplicates) console.error(`- ${label}`);
  process.exit(1);
}

console.log('Home mockup checks passed.');
