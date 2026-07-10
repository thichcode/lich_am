(function() {
  'use strict';

  const app = document.getElementById('app-content');
  const loading = document.getElementById('app-loading');

  function showLoading(visible) {
    loading.style.display = visible ? 'flex' : 'none';
    app.style.display = visible ? 'none' : 'block';
  }

  function render(data) {
    if (!data) return;

    document.getElementById('nav-year').textContent = 'Năm ' + data.year;

    document.getElementById('cal-weekday').textContent = data.weekday || '';
    document.getElementById('cal-day').textContent = data.day || '';
    document.getElementById('cal-monthyear').textContent = data.monthYear || '';

    const quoteEl = document.getElementById('quote-text');
    if (data.quote) quoteEl.textContent = data.quote;

    if (data.lunar) {
      document.getElementById('current-hour').textContent = 'Hiện tại: Giờ ' + (data.currentHourChi || '');
      document.getElementById('lunar-day').textContent = data.lunar.day;
      document.getElementById('lunar-month').textContent = 'THÁNG ' + data.lunar.monthName;
      document.getElementById('cc-year').textContent = data.yearCanChi || '';
      document.getElementById('cc-year-animal').textContent = data.yearAnimal ? '(' + data.yearAnimal + ')' : '';
      document.getElementById('cc-month').textContent = data.monthCanChi || '';
      document.getElementById('cc-month-animal').textContent = data.monthAnimal ? '(' + data.monthAnimal + ')' : '';
      document.getElementById('cc-day').textContent = data.dayCanChi || '';
      document.getElementById('cc-day-animal').textContent = data.dayAnimal ? '(' + data.dayAnimal + ')' : '';
    } else {
      document.getElementById('current-hour').textContent = '';
    }

    const hoursRow = document.getElementById('hours-row');
    if (data.goodHours && data.badHours) {
      hoursRow.style.display = 'flex';
      renderHours('good-hours-grid', data.goodHours, true);
      renderHours('bad-hours-grid', data.badHours, false);
    } else {
      hoursRow.style.display = 'none';
    }

    const activitiesCard = document.getElementById('activities-card');
    if (data.goodActivities || data.badActivities) {
      activitiesCard.style.display = 'block';
      document.getElementById('good-activities-text').textContent = data.goodActivities || '';
      document.getElementById('bad-activities-text').textContent = data.badActivities || '';
    } else {
      activitiesCard.style.display = 'none';
    }

    const clashCard = document.getElementById('clash-card');
    if (data.clashAges || data.hyThan) {
      clashCard.style.display = 'block';
      document.getElementById('clash-ages').textContent = data.clashAges || '';
      document.getElementById('direction-hy-than').textContent = data.hyThan ? 'Hỷ Thần: ' + data.hyThan : '';
      document.getElementById('direction-tai-than').textContent = data.taiThan ? 'Tài Thần: ' + data.taiThan : '';
    } else {
      clashCard.style.display = 'none';
    }

    const termCard = document.getElementById('term-card');
    if (data.termCurrent && data.termNext) {
      termCard.style.display = 'block';
      var tc = document.querySelector('#term-current .term-name');
      var td = document.querySelector('#term-current .term-date');
      if (tc) tc.textContent = data.termCurrent.name;
      if (td) td.textContent = data.termCurrent.date;
      var nc = document.querySelector('#term-next .term-name');
      var nd = document.querySelector('#term-next .term-date');
      if (nc) nc.textContent = data.termNext.name;
      if (nd) nd.textContent = data.termNext.date;
    } else {
      termCard.style.display = 'none';
    }

    const eventsCard = document.getElementById('events-card');
    if (data.events && data.events.length > 0) {
      eventsCard.style.display = 'block';
      var list = document.getElementById('events-list');
      list.innerHTML = '';
      data.events.forEach(function(e) {
        var item = document.createElement('div');
        item.className = 'event-item';
        item.innerHTML = '<span class="event-date">' + (e.date || '') + '</span><span class="event-name">' + (e.name || '') + '</span>';
        list.appendChild(item);
      });
    } else {
      eventsCard.style.display = 'none';
    }
  }

  function renderHours(gridId, hours, isGood) {
    var grid = document.getElementById(gridId);
    grid.innerHTML = '';
    var cls = isGood ? 'hour-chip-good' : 'hour-chip-bad';
    hours.forEach(function(h) {
      var chip = document.createElement('div');
      chip.className = 'hour-chip ' + cls;
      chip.innerHTML = '<span class="hour-chip-time">' + (h.time || '') + '</span><span class="hour-chip-name">Giờ ' + (h.chi || '') + '</span>';
      grid.appendChild(chip);
    });
  }

  renderHours('good-hours-grid', [], true);
  renderHours('bad-hours-grid', [], false);

  window.updateLunarData = function(jsonStr) {
    try {
      var data = JSON.parse(jsonStr);
      showLoading(false);
      render(data);
    } catch(e) {
      showLoading(false);
    }
  };

  if (typeof nativeApp !== 'undefined' && nativeApp.requestInitialData) {
    nativeApp.requestInitialData();
  }
})();
