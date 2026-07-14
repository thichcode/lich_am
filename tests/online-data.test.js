const test = require('node:test');
const assert = require('node:assert/strict');
const online = require('../app/src/main/assets/ui/default/assets/online-data.js');

test('parses a northern lottery item and its actual draw date', () => {
  const result = online.parseLotteryItem({
    title: 'KẾT QUẢ XỔ SỐ MIỀN BẮC NGÀY 13/07 (Thứ Hai)',
    link: 'https://xskt.com.vn/xsmb/ngay-13-7-2026',
    description: 'ĐB: 74299\n1: 93956\n2: 52860 - 61224\n7: 97 - 76 - 05 - 75',
    regionName: 'Miền Bắc',
    badge: 'MB',
    color: '#991B1B'
  });
  assert.equal(result.dateLabel, '13/07/2026');
  assert.equal(result.regions[0].name, 'Miền Bắc');
  assert.deepEqual(result.regions[0].prizes.slice(0, 3), [
    { label: 'ĐB', num: '74299', special: true },
    { label: 'G1', num: '93956', special: false },
    { label: 'G2', num: '52860 - 61224', special: false }
  ]);
});

test('splits southern provinces without treating markup as HTML', () => {
  const result = online.parseLotteryItem({
    title: 'KẾT QUẢ XỔ SỐ MIỀN NAM NGÀY 13/07 (Thứ Hai)',
    link: 'https://xskt.com.vn/xsmn/ngay-13-7-2026',
    description: '[Cà Mau]\nĐB: 303404\n1: 90382\n2: 93956\n7: 8748: 70\n[Đồng Tháp]\nĐB: 439097\n1: 10472',
    regionName: 'Miền Nam',
    badge: 'MN',
    color: '#B45309'
  });
  assert.deepEqual(result.regions.map((region) => region.name), ['Cà Mau', 'Đồng Tháp']);
  assert.equal(result.regions[1].prizes[0].num, '439097');
  assert.deepEqual(result.regions[0].prizes.slice(-2), [
    { label: 'G7', num: '8748', special: false },
    { label: 'G8', num: '70', special: false }
  ]);
  assert.ok(result.regions.every((region) => region.dateLabel === '13/07/2026'));
});
