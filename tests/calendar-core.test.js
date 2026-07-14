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
  const assessment = core.assessSolarDate(date);
  assert.deepEqual(assessment, core.assessSolarDate(date));
  assert.equal(typeof assessment.shouldDo, 'string');
  assert.ok(assessment.shouldDo.length > 0);
  const wedding = core.findGoodDays(7, 2026, 'cuoihoi', 3);
  const opening = core.findGoodDays(7, 2026, 'khaitruong', 3);
  const construction = core.findGoodDays(7, 2026, 'dongtho', 3);
  assert.ok(wedding.every((item) => item.assessment.category === 'cuoihoi'));
  assert.ok(construction.every((item) => item.assessment.category === 'dongtho'));
  const dates = (items) => items.map((item) => `${item.date.year}-${item.date.month}-${item.date.day}`);
  assert.notDeepEqual(dates(wedding), dates(opening));
  assert.notDeepEqual(dates(opening), dates(construction));
});

test('good-day results are calculated and sorted', () => {
  const results = core.findGoodDays(7, 2026, 'khaitruong', 3, 14);
  assert.equal(results.length, 3);
  assert.ok(results.every((item) => item.assessment.categorySuitable));
  assert.ok(results.every((item) => item.assessment.categoryLabel.startsWith('Phù hợp ')));
  assert.ok(results.every((item) => item.date.month !== 7 || item.date.day >= 14));
  assert.ok(results[0].jd < results[1].jd && results[1].jd < results[2].jd);
});

test('general assessments always contain activity guidance', () => {
  for (let day = 1; day <= 31; day += 1) {
    const assessment = core.assessSolarDate({ day, month: 7, year: 2026 });
    assert.equal(typeof assessment.shouldDo, 'string');
    assert.ok(assessment.shouldDo.length > 0);
    assert.equal(typeof assessment.shouldAvoid, 'string');
    assert.ok(assessment.shouldAvoid.length > 0);
  }
});
