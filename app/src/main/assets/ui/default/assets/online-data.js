(function (root, factory) {
  var api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  if (root) root.LichAmOnlineData = api;
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  function pad2(value) {
    return value < 10 ? '0' + value : String(value);
  }

  function drawDate(title, link) {
    var linkMatch = String(link || '').match(/ngay-(\d{1,2})-(\d{1,2})-(\d{4})/i);
    if (linkMatch) return pad2(Number(linkMatch[1])) + '/' + pad2(Number(linkMatch[2])) + '/' + linkMatch[3];
    var titleMatch = String(title || '').match(/NGÀY\s+(\d{1,2})\/(\d{1,2})(?:\/(\d{4}))?/i);
    if (!titleMatch || !titleMatch[3]) return '';
    return pad2(Number(titleMatch[1])) + '/' + pad2(Number(titleMatch[2])) + '/' + titleMatch[3];
  }

  function prizeLabel(raw) {
    return raw.toUpperCase() === 'ĐB' ? 'ĐB' : 'G' + raw;
  }

  function parseLotteryItem(item) {
    var dateLabel = drawDate(item.title, item.link);
    var regions = [];
    var current = { name: item.regionName, badge: item.badge, color: item.color, dateLabel: dateLabel, prizes: [] };
    var lines = String(item.description || '').split(/\r?\n/);

    function finishRegion() {
      if (current.prizes.length > 0) regions.push(current);
    }

    for (var i = 0; i < lines.length; i++) {
      var line = lines[i].trim();
      if (!line) continue;
      var province = line.match(/^\[([^\]]+)\]$/);
      if (province) {
        finishRegion();
        current = { name: province[1], badge: item.badge, color: item.color, dateLabel: dateLabel, prizes: [] };
        continue;
      }
      var prize = line.match(/^(ĐB|[1-8])\s*:\s*([0-9][0-9\s:-]*)$/i);
      if (!prize) continue;
      var combined = prize[1] === '7' && prize[2].match(/^(\d{4})\s*:\s*(\d{2})$/);
      if (combined) {
        current.prizes.push({ label: 'G7', num: combined[1], special: false });
        current.prizes.push({ label: 'G8', num: combined[2], special: false });
        continue;
      }
      current.prizes.push({
        label: prizeLabel(prize[1]),
        num: prize[2].trim(),
        special: prize[1].toUpperCase() === 'ĐB'
      });
    }
    finishRegion();

    return {
      dateLabel: dateLabel,
      regions: regions
    };
  }

  return { parseLotteryItem: parseLotteryItem };
});
