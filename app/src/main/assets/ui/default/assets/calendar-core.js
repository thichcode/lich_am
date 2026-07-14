(function (root, factory) {
  var api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  if (root) root.LichAmCore = api;
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  function INT(x) { return Math.floor(x); }

  function jdFromDate(dd, mm, yy) {
    var a = INT((14 - mm) / 12);
    var y = yy + 4800 - a;
    var m = mm + 12 * a - 3;
    var jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - INT(y / 100) + INT(y / 400) - 32045;
    if (jd < 2299161) jd = dd + INT((153 * m + 2) / 5) + 365 * y + INT(y / 4) - 32083;
    return jd;
  }

  function jdToDate(jd) {
    var a, b, c, d, e, m, day, month, year;
    if (jd > 2299160) {
      a = jd + 32044;
      b = INT((4 * a + 3) / 146097);
      c = a - INT((b * 146097) / 4);
    } else {
      b = 0;
      c = jd + 32082;
    }
    d = INT((4 * c + 3) / 1461);
    e = c - INT((1461 * d) / 4);
    m = INT((5 * e + 2) / 153);
    day = e - INT((153 * m + 2) / 5) + 1;
    month = m + 3 - 12 * INT(m / 10);
    year = b * 100 + d - 4800 + INT(m / 10);
    return [day, month, year];
  }

  function getNewMoonDay(k, timeZone) {
    var T = k / 1236.85;
    var T2 = T * T;
    var T3 = T2 * T;
    var dr = Math.PI / 180;
    var Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
    Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
    var M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
    var Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
    var F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000236 * T3;
    var C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
    C1 -= 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
    C1 -= 0.0004 * Math.sin(dr * 3 * Mpr) + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
    C1 -= 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
    C1 -= 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
    C1 += 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
    var deltat = T < -11
      ? 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3
      : -0.000278 + 0.000265 * T + 0.000262 * T2;
    return INT(Jd1 + C1 - deltat + 0.5 + timeZone / 24);
  }

  function getSunLongitude(jdn, timeZone) {
    var T = (jdn - 2451545.5 - timeZone / 24) / 36525;
    var T2 = T * T;
    var dr = Math.PI / 180;
    var M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
    var L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
    var DL = (1.91460 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M)
      + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M)
      + 0.000290 * Math.sin(dr * 3 * M);
    var L = (L0 + DL) * dr;
    L -= Math.PI * 2 * INT(L / (Math.PI * 2));
    if (L < 0) L += Math.PI * 2;
    return INT(L / Math.PI * 6);
  }

  function getLunarMonth11(yy, timeZone) {
    var off = jdFromDate(31, 12, yy) - 2415021;
    var k = INT(off / 29.530588853);
    var nm = getNewMoonDay(k, timeZone);
    if (getSunLongitude(nm, timeZone) >= 9) nm = getNewMoonDay(k - 1, timeZone);
    return nm;
  }

  function getLeapMonthOffset(a11, timeZone) {
    var k = INT((a11 - 2415021.076998695) / 29.530588853 + 0.5);
    var last = 0;
    var i = 1;
    var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
    do {
      last = arc;
      i++;
      arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
    } while (arc !== last && i < 14);
    return i - 1;
  }

  function convertSolar2Lunar(dd, mm, yy, timeZone) {
    var dayNumber = jdFromDate(dd, mm, yy);
    var k = INT((dayNumber - 2415021.076998695) / 29.530588853);
    var monthStart = getNewMoonDay(k + 1, timeZone);
    if (monthStart > dayNumber) monthStart = getNewMoonDay(k, timeZone);
    var a11 = getLunarMonth11(yy, timeZone);
    var b11 = a11;
    var lunarYear;
    if (a11 >= monthStart) {
      lunarYear = yy;
      a11 = getLunarMonth11(yy - 1, timeZone);
    } else {
      lunarYear = yy + 1;
      b11 = getLunarMonth11(yy + 1, timeZone);
    }
    var lunarDay = dayNumber - monthStart + 1;
    var diff = INT((monthStart - a11) / 29);
    var lunarLeap = 0;
    var lunarMonth = diff + 11;
    if (b11 - a11 > 365) {
      var leapMonthDiff = getLeapMonthOffset(a11, timeZone);
      if (diff >= leapMonthDiff) {
        lunarMonth = diff + 10;
        if (diff === leapMonthDiff) lunarLeap = 1;
      }
    }
    if (lunarMonth > 12) lunarMonth -= 12;
    if (lunarMonth >= 11 && diff < 4) lunarYear -= 1;
    return [lunarDay, lunarMonth, lunarYear, lunarLeap];
  }

  var CANS = ['Giáp', 'Ất', 'Bính', 'Đinh', 'Mậu', 'Kỷ', 'Canh', 'Tân', 'Nhâm', 'Quý'];
  var CHIS = ['Tý', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tỵ', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];
  var ANIMALS = ['Chuột', 'Trâu', 'Hổ', 'Mèo', 'Rồng', 'Rắn', 'Ngựa', 'Dê', 'Khỉ', 'Gà', 'Chó', 'Lợn'];

  function getYearCanChi(lunarYear) {
    var canIndex = (lunarYear + 6) % 10;
    var chiIndex = (lunarYear + 8) % 12;
    return { text: CANS[canIndex] + ' ' + CHIS[chiIndex], animal: ANIMALS[chiIndex] };
  }

  function getMonthCanChi(lunarYear, lunarMonth) {
    var yearCanIndex = (lunarYear + 6) % 10;
    var canIndex = (yearCanIndex * 2 + lunarMonth + 1) % 10;
    var chiIndex = (lunarMonth + 1) % 12;
    return { text: CANS[canIndex] + ' ' + CHIS[chiIndex], animal: ANIMALS[chiIndex] };
  }

  function getDayCanChi(jd) {
    var canIndex = (jd + 9) % 10;
    var chiIndex = (jd + 1) % 12;
    return { text: CANS[canIndex] + ' ' + CHIS[chiIndex], animal: ANIMALS[chiIndex] };
  }

  function getHoursForDay(chiIndex) {
    var good = [];
    if (chiIndex === 0 || chiIndex === 6) good = [0, 1, 3, 6, 8, 9];
    else if (chiIndex === 1 || chiIndex === 7) good = [2, 3, 5, 8, 10, 11];
    else if (chiIndex === 2 || chiIndex === 8) good = [0, 1, 4, 5, 7, 10];
    else if (chiIndex === 3 || chiIndex === 9) good = [0, 2, 3, 6, 7, 9];
    else if (chiIndex === 4 || chiIndex === 10) good = [2, 4, 5, 8, 9, 11];
    else if (chiIndex === 5 || chiIndex === 11) good = [1, 4, 6, 7, 10, 11];
    var allHours = [
      { t: '23h-01h', l: 'Tý' }, { t: '01h-03h', l: 'Sửu' },
      { t: '03h-05h', l: 'Dần' }, { t: '05h-07h', l: 'Mão' },
      { t: '07h-09h', l: 'Thìn' }, { t: '09h-11h', l: 'Tỵ' },
      { t: '11h-13h', l: 'Ngọ' }, { t: '13h-15h', l: 'Mùi' },
      { t: '15h-17h', l: 'Thân' }, { t: '17h-19h', l: 'Dậu' },
      { t: '19h-21h', l: 'Tuất' }, { t: '21h-23h', l: 'Hợi' }
    ];
    var goodHours = [];
    var badHours = [];
    for (var i = 0; i < 12; i++) {
      if (good.indexOf(i) >= 0) goodHours.push(allHours[i]);
      else badHours.push(allHours[i]);
    }
    return { goodHours: goodHours, badHours: badHours };
  }

  function getHourChiIndex(hour) {
    if (!Number.isInteger(hour) || hour < 0 || hour > 23) throw new RangeError('hour must be 0..23');
    return Math.floor(((hour + 1) % 24) / 2);
  }

  var SOLAR_TERMS = [
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

  function pad2(value) {
    return value < 10 ? '0' + value : String(value);
  }

  function getSolarTerm(day, month, year) {
    var currentIndex = -1;
    for (var i = 0; i < SOLAR_TERMS.length; i++) {
      if (month > SOLAR_TERMS[i].m || (month === SOLAR_TERMS[i].m && day >= SOLAR_TERMS[i].d)) currentIndex = i;
    }
    var currentYear = year;
    if (currentIndex === -1) {
      currentIndex = SOLAR_TERMS.length - 1;
      currentYear = year - 1;
    }
    var nextIndex = (currentIndex + 1) % SOLAR_TERMS.length;
    var nextYear = nextIndex === 0 ? currentYear + 1 : currentYear;
    return {
      current: SOLAR_TERMS[currentIndex].n,
      currentStart: pad2(SOLAR_TERMS[currentIndex].d) + '/' + pad2(SOLAR_TERMS[currentIndex].m),
      currentYear: currentYear,
      next: SOLAR_TERMS[nextIndex].n,
      nextStart: pad2(SOLAR_TERMS[nextIndex].d) + '/' + pad2(SOLAR_TERMS[nextIndex].m),
      nextYear: nextYear
    };
  }

  var TRUC_NAMES = ['Kiến', 'Trừ', 'Mãn', 'Bình', 'Định', 'Chấp', 'Phá', 'Nguy', 'Thành', 'Thu', 'Khai', 'Bế'];
  var GOOD_TRUC = [2, 3, 4, 8, 10];
  var BAD_TRUC = [1, 6, 7, 11];
  var CATEGORY_TRUC = {
    cuoihoi: [3, 4, 8],
    khaitruong: [2, 9, 10],
    dongtho: [0, 2, 4]
  };
  var CATEGORY_GOOD_TEXT = {
    cuoihoi: 'Cưới hỏi, lễ hỏi, gặp mặt hai gia đình',
    khaitruong: 'Khai trương, mở hàng, ký kết',
    dongtho: 'Động thổ, sửa nhà, khởi công'
  };
  var CATEGORY_LABEL = {
    cuoihoi: 'Phù hợp cưới hỏi',
    khaitruong: 'Phù hợp khai trương',
    dongtho: 'Phù hợp làm nhà'
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
  var TAM_NUONG = [3, 7, 13, 18, 22, 27];
  var NGUYET_KY = [5, 14, 23];
  var DUONG_CONG_KY = ['13/1', '11/2', '9/3', '7/4', '5/5', '3/6', '1/7', '29/7', '27/8', '25/9', '23/10', '21/11', '19/12'];

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
    var isTamNuong = TAM_NUONG.indexOf(lunar[0]) >= 0;
    var isNguyetKy = NGUYET_KY.indexOf(lunar[0]) >= 0;
    var isDuongCongKy = DUONG_CONG_KY.indexOf(lunar[0] + '/' + lunar[1]) >= 0;
    if (isTamNuong) score -= 30;
    if (isNguyetKy) score -= 25;
    if (isDuongCongKy) score -= 35;
    if (lunar[0] === 1) score += 8;
    if (lunar[0] === 15) score += 10;
    var hasCategory = Object.prototype.hasOwnProperty.call(CATEGORY_TRUC, category);
    var allowed = hasCategory ? CATEGORY_TRUC[category] : [];
    var categorySuitable = hasCategory && allowed.indexOf(trucIndex) >= 0 && !isTamNuong && !isNguyetKy && !isDuongCongKy;
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
      categoryLabel: categorySuitable ? CATEGORY_LABEL[category] : '',
      shouldDo: categorySuitable ? CATEGORY_GOOD_TEXT[category] : TRUC_GOOD_TEXT[trucIndex],
      shouldAvoid: TRUC_BAD_TEXT[trucIndex]
    };
  }

  function findGoodDays(month, year, category, limit, startDay) {
    var result = [];
    var cursor = new Date(year, month - 1, startDay || 1);
    for (var scanned = 0; scanned < 93 && result.length < limit; scanned++) {
      var date = { day: cursor.getDate(), month: cursor.getMonth() + 1, year: cursor.getFullYear() };
      var assessment = assessSolarDate(date, category);
      if (assessment.categorySuitable) result.push({ date: date, jd: assessment.jd, assessment: assessment });
      cursor.setDate(cursor.getDate() + 1);
    }
    return result;
  }

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
    getHoursForDay: getHoursForDay,
    getHourChiIndex: getHourChiIndex,
    getSolarTerm: getSolarTerm,
    assessSolarDate: assessSolarDate,
    findGoodDays: findGoodDays
  };
});
