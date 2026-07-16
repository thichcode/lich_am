'use strict'
var CANS=LichAmCore.CANS
var CHIS=LichAmCore.CHIS
var ANIMALS=LichAmCore.ANIMALS
var jdFromDate=LichAmCore.jdFromDate
var convertSolar2Lunar=LichAmCore.convertSolar2Lunar
var getYearCanChi=LichAmCore.getYearCanChi
var getMonthCanChi=LichAmCore.getMonthCanChi
var getDayCanChi=LichAmCore.getDayCanChi
var getHoursForDay=LichAmCore.getHoursForDay
function appendTextElement(parent,tag,className,text){
  var element=document.createElement(tag)
  element.className=className
  element.textContent=String(text==null?'':text)
  parent.appendChild(element)
  return element
}
function padNumber(value){return value<10?'0'+value:String(value)}
function localDateString(date){return date.getFullYear()+'-'+padNumber(date.getMonth()+1)+'-'+padNumber(date.getDate())}
function parseLocalDate(value){
  var parts=String(value||'').split('-')
  if(parts.length!==3)return new Date()
  var date=new Date(Number(parts[0]),Number(parts[1])-1,Number(parts[2]))
  return isNaN(date.getTime())?new Date():date
}
function rssProxyUrl(remoteUrl){return 'https://appassets.androidplatform.net/rss-proxy?url='+encodeURIComponent(remoteUrl)}
function renderOnlineState(container,state,message,retry){
  container.innerHTML=''
  var box=document.createElement('div');box.className='online-state online-state-'+state
  appendTextElement(box,'div','online-state-message',message)
  if(retry){
    var button=document.createElement('button');button.type='button';button.className='online-retry';button.textContent='Thử lại'
    button.addEventListener('click',retry);box.appendChild(button)
  }
  container.appendChild(box)
}
function getElderDayData(dateStr){
  var d=parseLocalDate(dateStr)
  var day=d.getDate(),month=d.getMonth()+1,year=d.getFullYear()
  var wd=['CHỦ NHẬT','THỨ HAI','THỨ BA','THỨ TƯ','THỨ NĂM','THỨ SÁU','THỨ BẢY']
  var ld=convertSolar2Lunar(day,month,year,7)
  var ly=ld[0],lm=ld[1],lyr=ld[2],ll=ld[3],yc=getYearCanChi(lyr),mc=getMonthCanChi(lyr,lm),jd=jdFromDate(day,month,year),dc=getDayCanChi(jd)
  var assessment=LichAmCore.assessSolarDate({day:day,month:month,year:year}),quotes=['Một cây làm chẳng nên non ba cây chụm lại nên hòn núi cao.','Kính lão đắc thọ, trọng già già để tuổi cho.','Ăn quả nhớ kẻ trồng cây, ăn khoai nhớ kẻ cho dây mà trồng.','Con người có tổ có tông, như cây có cội như sông có nguồn.','Lá rụng về cội, nước chảy về nguồn.','Gieo hành vi gặt thói quen, gieo thói quen gặt tính cách.']
  var qi=(day+month)%quotes.length,dci=(jd+1)%12,hrs=getHoursForDay(dci)
  var cm={'Chuột':'Mậu Ngọ, Nhâm Ngọ, Canh Tý','Trâu':'Kỷ Mùi, Quý Mùi, Tân Sửu','Hổ':'Canh Thân, Giáp Thân, Mậu Dần','Mèo':'Tân Dậu, Ất Dậu, Kỷ Mão','Rồng':'Nhâm Tuất, Bính Tuất, Giáp Thìn','Rắn':'Quý Hợi, Đinh Hợi, Ất Tỵ','Ngựa':'Nhâm Tý, Bính Tý, Giáp Ngọ','Dê':'Quý Sửu, Đinh Sửu, Ất Mùi','Khỉ':'Mậu Dần, Bính Dần, Canh Thần','Gà':'Kỷ Mão, Đinh Mão, Tân Dậu','Chó':'Canh Thìn, Bính Thìn, Mậu Tuất','Hợi':'Tân Tỵ, Đinh Tỵ, Kỷ Hợi'}
  var ca=cm[dc.animal]||'Xung khắc các tuổi Thân, Dần',term=LichAmCore.getSolarTerm(day,month,year)
  return{solar:{day:day,month:'Tháng '+month,year:year,weekday:wd[d.getDay()]},lunar:{day:ly,month:'Tháng '+(lm<10?'0':'')+lm+(ll?' (Nhuận)':''),yearChi:yc.text,monthChi:mc.text,dayChi:dc.text,yearAnimal:yc.animal,monthAnimal:mc.animal,dayAnimal:dc.animal},isGoodDay:assessment.isGood,starText:assessment.label,quote:quotes[qi],goodHours:hrs.goodHours,badHours:hrs.badHours,shouldDo:assessment.shouldDo,shouldAvoid:assessment.shouldAvoid,clashAges:ca,solarTerm:term.current,termStart:term.currentStart+'/'+term.currentYear,nextTerm:term.next,nextTermStart:term.nextStart+'/'+term.nextYear}
}
// ===== STATE =====
var initDate=new Date(),lastKnownToday=localDateString(initDate),currentDate=lastKnownToday,currentTab='home',currentMonthView=initDate.getMonth()+1,currentYearView=initDate.getFullYear(),currentGoodDayCategory='cuoihoi',isBellEnabled=false,isHighContrast=false,isAutoScrolling=false,scrollTimer=null,timeRefreshTimer=null,currentPrayerFontSize=18
// ===== TOAST =====
function showElderToast(text){var t=document.getElementById('elder-toast'),tt=document.getElementById('elder-toast-text');if(!t||!tt)return;tt.innerText=text;t.classList.add('show');setTimeout(function(){t.classList.remove('show')},2000)}
// ===== CONTRAST =====
function toggleElderContrast(){isHighContrast=!isHighContrast;var ic=document.getElementById('contrastIcon'),lb=document.getElementById('contrast-label');if(isHighContrast){document.body.classList.add('dark');ic.innerText='☀️';lb.innerText='Giao diện tối đang bật';showElderToast('Đã bật giao diện dịu mắt')}else{document.body.classList.remove('dark');ic.innerText='🌓';lb.innerText='Giao diện tối dịu nhẹ';showElderToast('Đã về giao diện ấm áp')}}
// ===== NAVIGATION =====
function navigateDay(offset){
  var d=parseLocalDate(currentDate);d.setDate(d.getDate()+offset)
  currentDate=localDateString(d)
  currentMonthView=d.getMonth()+1;currentYearView=d.getFullYear()
  if(isBellEnabled)playBell()
  renderDayView()
  if(typeof nativeApp!=='undefined'&&nativeApp.changeDate)nativeApp.changeDate(offset)
  showElderToast('Đã chuyển tới ngày '+padNumber(d.getDate())+'/'+padNumber(d.getMonth()+1))
}
function resetToSampleDate(){
  var t=new Date()
  currentDate=localDateString(t);lastKnownToday=currentDate
  currentMonthView=t.getMonth()+1;currentYearView=t.getFullYear()
  if(isBellEnabled)playBell()
  renderDayView();switchTab('home')
  if(typeof nativeApp!=='undefined'&&nativeApp.changeDate)nativeApp.changeDate(0)
  showElderToast('Quay về hôm nay')
}
function handleDatePicked(val){
  currentDate=val;var p=val.split('-');currentMonthView=parseInt(p[1]);currentYearView=parseInt(p[0])
  if(isBellEnabled)playBell()
  renderDayView();switchTab('home')
  if(typeof nativeApp!=='undefined'&&nativeApp.changeDate)nativeApp.changeDate(0)
  showElderToast('Đã chọn: '+p[2]+'/'+p[1])
}
// ===== BELL =====
function playBell(){if(!isBellEnabled)return}
function toggleBellEffect(){isBellEnabled=!isBellEnabled;var t=document.getElementById('bell-toggle-indicator'),b=t?t.querySelector('div'):null;if(b){if(isBellEnabled){t.style.background='#064E3B';b.style.right='2px';b.style.left='auto';showElderToast('Đã bật tiếng Khánh đồng')}else{t.style.background='#D1D5DB';b.style.left='2px';b.style.right='auto';showElderToast('Đã tắt tiếng chuông')}}}
// ===== RENDER DAY VIEW =====
function renderDayView(){
  var data=getElderDayData(currentDate)
  document.getElementById('solar-weekday').innerHTML=data.solar.weekday.replace(' ','<br>')
  document.getElementById('solar-month-year').innerHTML=data.solar.month+'<br>'+data.solar.year
  document.getElementById('solar-day').innerText=data.solar.day
  document.getElementById('label-current-year').innerText='Năm '+data.solar.year
  document.getElementById('daily-quote').innerText=data.quote
  var hci=LichAmCore.getHourChiIndex(new Date().getHours())
  document.getElementById('current-hour-label').innerText='Hiện tại: Giờ '+CHIS[hci]
  document.getElementById('lunar-day-large').innerText=data.lunar.day
  document.getElementById('lunar-month').innerText=data.lunar.month.toUpperCase()
  document.getElementById('lunar-year-chi').innerHTML=data.lunar.yearChi.split(' ')[0]+' '+data.lunar.yearChi.split(' ')[1]+'<br><span class="canchi-animal">('+data.lunar.yearAnimal+')</span>'
  document.getElementById('lunar-month-chi').innerHTML=data.lunar.monthChi.split(' ')[0]+' '+data.lunar.monthChi.split(' ')[1]+'<br><span class="canchi-animal">('+data.lunar.monthAnimal+')</span>'
  document.getElementById('lunar-day-chi').innerHTML=data.lunar.dayChi.split(' ')[0]+' '+data.lunar.dayChi.split(' ')[1]+'<br><span class="canchi-animal">('+data.lunar.dayAnimal+')</span>'
  var gl=document.getElementById('good-hours');gl.innerHTML=data.goodHours.map(function(h){return'<div class="hour-chip hour-chip-good"><span class="hour-time">'+h.t+'</span><span class="hour-name hour-name-good">Giờ '+h.l+'</span></div>'}).join('')
  var bl=document.getElementById('bad-hours');bl.innerHTML=data.badHours.map(function(h){return'<div class="hour-chip hour-chip-bad"><span class="hour-time">'+h.t+'</span><span class="hour-name hour-name-bad">Giờ '+h.l+'</span></div>'}).join('')
  document.getElementById('should-do').innerText=data.shouldDo
  document.getElementById('should-avoid').innerText=data.shouldAvoid
  document.getElementById('clash-ages').innerText=data.clashAges
  var canIdx=CANS.indexOf(data.lunar.dayChi.split(' ')[0]);if(canIdx===-1)canIdx=0
  var hyThan=['Đông Bắc','Đông Bắc','Chính Tây','Chính Tây','Chính Đông','Chính Đông','Chính Nam','Chính Nam','Tây Bắc','Tây Bắc']
  var taiThan=['Đông Nam','Đông Nam','Chính Tây','Chính Tây','Chính Đông','Chính Đông','Chính Nam','Chính Nam','Tây Bắc','Tây Bắc']
  document.getElementById('direction-hy-than').innerText='Hỷ Thần: '+(hyThan[canIdx]||'')
  document.getElementById('direction-tai-than').innerText='Tài Thần: '+(taiThan[canIdx]||'')
  document.getElementById('current-solar-term').innerText=data.solarTerm
  document.getElementById('term-start-date').innerText='Bắt đầu: '+data.termStart
  document.getElementById('next-solar-term').innerText=data.nextTerm
  document.getElementById('next-term-start-date').innerText='Dự kiến: '+data.nextTermStart
}
// ===== MONTH VIEW =====
function changeMonth(offset){currentMonthView+=offset;if(currentMonthView>12){currentMonthView=1;currentYearView+=1}else if(currentMonthView<1){currentMonthView=12;currentYearView-=1}renderMonthView()}
function renderMonthView(){
  renderNews()
  document.getElementById('calendar-month-title').innerText='Tháng '+currentMonthView+' - '+currentYearView
  var fd=new Date(currentYearView,currentMonthView-1,1),so=(fd.getDay()+7)%7,td=new Date(currentYearView,currentMonthView,0).getDate()
  var grid=document.getElementById('calendar-grid-days');grid.innerHTML=''
  for(var i=0;i<so;i++)grid.innerHTML+='<div></div>'
  var today=new Date(),act=parseLocalDate(currentDate)
  for(var d=1;d<=td;d++){
    var ds=currentYearView+'-'+padNumber(currentMonthView)+'-'+padNumber(d),dd=getElderDayData(ds)
    var sel=act.getDate()===d&&act.getMonth()+1===currentMonthView&&act.getFullYear()===currentYearView
    var isT=today.getDate()===d&&today.getMonth()+1===currentMonthView&&today.getFullYear()===currentYearView
    var bg='background:#F3F4F6;color:#1F2937'
    if(sel)bg='background:#064E3B;color:#fff;border:3px solid #FBBF24'
    else if(isT)bg='background:#991B1B;color:#fff;border:3px solid #FBBF24'
    else if(dd.isGoodDay)bg='background:#D1FAE5;color:#064E3B;border:2px solid #6EE7B7'
    grid.innerHTML+='<button onclick="handleDatePicked(\''+ds+'\')" class="month-grid-cell" style="'+bg+'"><span class="month-grid-day">'+d+'</span><span class="month-grid-lunar">'+dd.lunar.day+'</span></button>'
  }
}
// ===== GOOD DAYS =====
function filterGoodDays(type){
  currentGoodDayCategory=type
  var cats=['cuoihoi','khaitruong','dongtho']
  cats.forEach(function(c){var b=document.getElementById('btn-f-'+c);if(c===type)b.style.cssText='flex:1;padding:10px 4px;border-radius:10px;font-size:clamp(11px,3vw,13px);font-weight:900;text-align:center;border:2px solid #B45309;background:#064E3B;color:#fff;cursor:pointer';else b.style.cssText='flex:1;padding:10px 4px;border-radius:10px;font-size:clamp(11px,3vw,13px);font-weight:900;text-align:center;border:2px solid transparent;background:#E5E7EB;color:#1F2937;cursor:pointer'})
  var now=new Date(),startDay=currentMonthView===now.getMonth()+1&&currentYearView===now.getFullYear()?now.getDate():1
  var list=document.getElementById('good-days-list'),results=LichAmCore.findGoodDays(currentMonthView,currentYearView,type,3,startDay)
  list.innerHTML=''
  results.forEach(function(item){
    var row=document.createElement('div');row.className='good-day-item'
    var badge=document.createElement('div');badge.className='good-day-badge'
    appendTextElement(badge,'span','good-day-num',item.date.day)
    appendTextElement(badge,'span','good-day-month','THÁNG '+item.date.month)
    var info=document.createElement('div');info.className='good-day-info'
    appendTextElement(info,'div','good-day-desc',item.assessment.dayCanChi.text+' - '+item.assessment.categoryLabel)
    appendTextElement(info,'div','good-day-lunar','Âm: '+item.assessment.lunar[0]+'/'+item.assessment.lunar[1])
    appendTextElement(info,'div','good-day-sub',item.assessment.shouldDo)
    row.appendChild(badge);row.appendChild(info);list.appendChild(row)
  })
}
// ===== LOTTERY =====
function renderLottery() {
  var el=document.getElementById('lottery-body')
  renderOnlineState(el,'loading','Đang tải kết quả xổ số...')

  function renderRegions(regions){
    el.innerHTML=''
    regions.forEach(function(region){
      var regionElement=document.createElement('div');regionElement.className='lottery-region'
      var title=document.createElement('div');title.className='lottery-region-title'
      var badge=document.createElement('span');badge.className='badge';badge.style.background=region.color;badge.textContent=region.badge
      title.appendChild(badge);title.appendChild(document.createTextNode(' '+region.name+' - '+region.dateLabel));regionElement.appendChild(title)
      var prizes=document.createElement('div');prizes.className='lottery-prizes'
      region.prizes.forEach(function(prize){
        var prizeElement=document.createElement('span');prizeElement.className='lottery-prize'+(prize.special?' lottery-prize-special':'')
        var label=document.createElement('span');label.className=region.name==='Miền Bắc'?'red':'green';label.textContent=prize.label
        prizeElement.appendChild(label);prizeElement.appendChild(document.createTextNode(' '+prize.num));prizes.appendChild(prizeElement)
      })
      regionElement.appendChild(prizes);el.appendChild(regionElement)
    })
    appendTextElement(el,'div','lottery-date','Ngày quay được ghi riêng theo từng khu vực')
  }

  function parseFeed(xmlDoc,definition){
    var item=xmlDoc.querySelector('item')
    if(!item)throw Error('Lottery feed is empty')
    var title=item.querySelector('title'),description=item.querySelector('description'),link=item.querySelector('link')
    if(!title||!description||!link)throw Error('Lottery feed item is incomplete')
    var parsed=LichAmOnlineData.parseLotteryItem({
      title:title.textContent,
      description:description.textContent,
      link:link.textContent,
      regionName:definition.name,
      badge:definition.badge,
      color:definition.color
    })
    if(!parsed.dateLabel||parsed.regions.length===0)throw Error('Lottery feed item is invalid')
    return parsed
  }

  var regionDefs=[
    {name:'Miền Bắc',badge:'MB',color:'#991B1B',url:'https://xskt.com.vn/rss-feed/mien-bac-xsmb.rss'},
    {name:'Miền Trung',badge:'MT',color:'#064E3B',url:'https://xskt.com.vn/rss-feed/mien-trung-xsmt.rss'},
    {name:'Miền Nam',badge:'MN',color:'#B45309',url:'https://xskt.com.vn/rss-feed/mien-nam-xsmn.rss'}
  ]

  var allRegions=[],pendingCount=regionDefs.length,isError=false
  regionDefs.forEach(function(definition){
    fetch(rssProxyUrl(definition.url))
      .then(function(response){if(!response.ok)throw Error();return response.text()})
      .then(function(xml){return parseFeed(new DOMParser().parseFromString(xml,'text/xml'),definition)})
      .then(function(result){
        allRegions=allRegions.concat(result.regions);pendingCount--
        if(pendingCount===0&&!isError){isError=true;renderRegions(allRegions)}
      })
      .catch(function(){
        pendingCount--
        if(pendingCount===0&&!isError){isError=true
          if(allRegions.length>0)renderRegions(allRegions)
          else renderOnlineState(el,'error','Không tải được kết quả xổ số. Vui lòng kiểm tra mạng và thử lại.',renderLottery)
        }
      })
  })
}
// ===== NEWS =====
function renderNews(){
  var el=document.getElementById('news-list')
  renderOnlineState(el,'loading','Đang tải tin tức...')
  fetch(rssProxyUrl('https://vnexpress.net/rss/tin-moi-nhat.rss')).then(function(r){if(!r.ok)throw Error();return r.text()}).then(function(xml){
    var doc=new DOMParser().parseFromString(xml,'text/xml'),items=doc.querySelectorAll('item'),articles=[],colors=['#D1FAE5','#FEE2E2','#DBEAFE']
    var catMap={kinh:'Kinh tế',tai:'Kinh tế',chung:'Kinh tế',xuat:'Kinh tế',giao:'Kinh tế',chinh:'Chính trị',thoi:'Chính trị',xa:'Chính trị','phap-luat':'Chính trị',the:'Thể thao','bong-da':'Thể thao',cong:'Công nghệ',suc:'Sức khỏe',giai:'Giải trí',van:'Văn hóa',du:'Du lịch',gia:'Giáo dục',khoa:'Khoa học','nha-dat':'Nhà đất','oto-xe-may':'Xe',ban:'Bạn đọc',tam:'Tâm sự',cuoi:'Cười'}
    var catColors={kinh:'#064E3B',chinh:'#991B1B',the:'#2563EB',cong:'#6D28D9',suc:'#059669',giai:'#D97706',van:'#7C3AED',du:'#0891B2',gia:'#0D9488',khoa:'#4F46E5',oto:'#57534E',nha:'#92400E',ban:'#1F2937',tam:'#9D174D',cuoi:'#A16207'}
    for(var i=0;i<items.length&&i<10;i++){
      var item=items[i]
      var title=item.querySelector('title')?item.querySelector('title').textContent:''
      var link=item.querySelector('link')?item.querySelector('link').textContent:''
      var catText=item.querySelector('category')?item.querySelector('category').textContent:''
      var pubDate=item.querySelector('pubDate')?item.querySelector('pubDate').textContent:''
      var catName='',catColor='#064E3B'
      for(var k in catMap){if(catText.indexOf(k)>-1){catName=catMap[k];catColor=catColors[k]||'#064E3B';break}}
      if(!catName){catName='Tin tức';catColor='#064E3B'}
      articles.push({title:title,link:link,category:catName,color:catColor,published:pubDate.substring(0,16),background:colors[i%3]})
    }
    if(articles.length===0)throw Error()
    el.innerHTML=''
    articles.forEach(function(article){
      var row=document.createElement('div');row.className='news-article'
      var thumb=document.createElement('div');thumb.className='news-article-thumb';thumb.style.background=article.background;thumb.textContent='📰'
      var info=document.createElement('div');info.className='news-article-info'
      var titleElement=document.createElement('div');titleElement.className='news-article-title';titleElement.textContent=article.title
      var source=document.createElement('div');source.className='news-article-source'
      var category=document.createElement('span');category.className='news-article-cat';category.style.background=article.color;category.textContent=article.category
      source.appendChild(category);appendTextElement(source,'span','','VnExpress');appendTextElement(source,'span','news-article-date','🔗 '+article.published)
      info.appendChild(titleElement);info.appendChild(source)
      if(article.link){
        var linkBtn=document.createElement('span');linkBtn.className='news-link';linkBtn.textContent='Xem thêm →'
        linkBtn.addEventListener('click',function(e){
          e.stopPropagation()
          if(typeof nativeApp!=='undefined'&&nativeApp.openUrl)nativeApp.openUrl(article.link)
          else showElderAlert(article.title)
        })
        info.appendChild(linkBtn)
      }
      row.addEventListener('click',function(){showElderAlert(article.title)})
      row.appendChild(thumb);row.appendChild(info);el.appendChild(row)
    })
  }).catch(function(){
    renderOnlineState(el,'error','Không tải được tin tức. Vui lòng kiểm tra mạng và thử lại.',renderNews)
  })
}
// ===== PRAYERS =====
var prayersDB=[{id:'mungone',title:'Khấn Mùng Một & Ngày Rằm',category:'Gia Tiên',body:'Nam mô A Di Đà Phật! (3 lần, lạy 3 lần)<br><br>Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.<br>Con kính lạy Hoàng thiên Hậu Thổ chư vị Tôn thần.<br>Con kính lạy ngài Bản cảnh Thành hoàng, ngài Bản xứ Thổ địa, ngài Bản gia Táo quân cùng chư vị Thần linh.<br>Con kính lạy Tổ Tiên, Hiển khảo, Hiển Tỷ, hương linh nội ngoại tông tộc nội tộc dâng hiến kính lễ.<br><br>Hôm nay là ngày Mùng Một (hoặc ngày Rằm) năm Bính Ngọ.<br>Chúng con thành tâm chuẩn bị lễ hoa, trà quả dâng lên ban thờ tiên tổ tiên cốt phù độ trì.<br>Cầu xin chư vị thần linh nâng đỡ bảo hộ gia đình bình an, tai qua nạn khỏi, bệnh tật tiêu trừ, gia đạo ấm êm.<br>Hương linh tổ tiên rộng lòng bao dung cho lỗi lầm con cháu.<br><br>Nam mô A Di Đà Phật! (3 lần, lạy 3 lần).'},{id:'giatien',title:'Khấn Tổ Tiên ngày Giỗ',category:'Gia Tiên',body:'Nam mô A Di Đà Phật! (3 lần, lạy 3 lần)<br><br>Kính lạy chư vị Thần linh Thổ công thổ địa lai lâm chứng giám dâng hiến.<br>Kính lạy vong linh Tổ tiên dòng họ nội ngoại tôn kính.<br><br>Hôm nay gia đình chúng con sửa soạn mâm cơm tươm tất kính dâng ngày giỗ tôn kính của... vong linh tôn kính.<br>Cầu mong vong linh sớm siêu sinh cực lạc, hưởng nhang khói thành kính của con cháu lâu đời.<br>Phù trì cho toàn thể gia quyến sức khỏe, thịnh vượng bền lâu.<br><br>Nam mô A Di Đà Phật! (3 lần, lạy 3 lần).'},{id:'giao-thua',title:'Khấn Giao Thừa',category:'Tết Cổ Truyền',body:'Nam mô A Di Đà Phật! (3 lần, lạy 3 lần)<br><br>Kính lạy cựu niên quan đương niên hành khiển thần quân tôn kính.<br>Kính lạy tân niên quan đại vương tối cao.<br><br>Phút giây thiêng liêng giao thừa đất trời chuyển hóa, chúng con thành tâm dâng hương trà quả lễ vật tạ ơn trời đất phật thánh thần tiên độ trì suốt một năm bình an ấm áp đã qua.<br>Mong cầu năm mới gia hộ bình an, dồi dào khí lực, muôn sự hanh thông.<br><br>Nam mô A Di Đà Phật! (3 lần, lạy 3 lạy).'}]
var prayerSearchKey=''
function renderPrayersList(){
  var c=document.getElementById('prayers-list-container');c.innerHTML=''
  var f=prayersDB.filter(function(p){return p.title.toLowerCase().includes(prayerSearchKey.toLowerCase())})
  if(f.length===0){c.innerHTML='<div style="text-align:center;padding:20px;color:#9CA3AF;font-weight:700;font-size:14px">Không tìm thấy bài khấn.</div>';return}
  c.innerHTML=f.map(function(p){return'<div class="prayer-item" onclick="openPrayerReader(\''+p.id+'\')"><div class="prayer-icon">📚</div><div class="prayer-info"><div class="prayer-category">'+p.category+'</div><div class="prayer-title">'+p.title+'</div></div><span class="prayer-arrow">▶</span></div>'}).join('')
}
function searchPrayers(q){prayerSearchKey=q;renderPrayersList()}
function resetPrayerFilter(){document.getElementById('prayerSearchInput').value='';prayerSearchKey='';renderPrayersList()}
function openPrayerReader(id){var p=prayersDB.find(function(i){return i.id===id});if(!p)return;document.getElementById('prayer-reader-title').innerText=p.title;document.getElementById('prayer-text-scroll').innerHTML=p.body;document.getElementById('prayer-text-scroll').style.fontSize=currentPrayerFontSize+'px';document.getElementById('prayer-reader-modal').classList.add('open');if(isBellEnabled)playBell()}
function closePrayerReader(){document.getElementById('prayer-reader-modal').classList.remove('open');stopAutoScroll()}
function adjustFontSize(offset){currentPrayerFontSize+=offset;if(currentPrayerFontSize<14)currentPrayerFontSize=14;if(currentPrayerFontSize>32)currentPrayerFontSize=32;document.getElementById('prayer-text-scroll').style.fontSize=currentPrayerFontSize+'px';showElderToast('Cỡ chữ: '+currentPrayerFontSize+'px')}
function toggleAutoScroll(){var btn=document.getElementById('autoscroll-btn'),box=document.getElementById('prayer-text-scroll');if(isAutoScrolling){stopAutoScroll()}else{isAutoScrolling=true;btn.innerText='TẠM DỪNG';btn.style.background='#991B1B';scrollTimer=setInterval(function(){box.scrollTop+=1;if(box.scrollTop+box.clientHeight>=box.scrollHeight)stopAutoScroll()},100)}}
function stopAutoScroll(){var btn=document.getElementById('autoscroll-btn');if(btn){btn.innerText='BẬT';btn.style.background='#064E3B'}isAutoScrolling=false;clearInterval(scrollTimer)}
// ===== TAB SWITCHING =====
function switchTab(tabId){
  currentTab=tabId;var tabs=['home','month','good-days','prayers','more']
  tabs.forEach(function(t){document.getElementById('tab-'+t).style.display=(t===tabId)?'block':'none'})
  tabs.forEach(function(t){var btn=document.getElementById('nav-'+t),icon=btn.querySelector('.nav-icon'),label=btn.querySelector('.nav-label');if(t===tabId){icon.className='nav-icon nav-icon-active';label.style.color='#064E3B'}else{icon.className='nav-icon nav-icon-inactive';label.style.color='#78716C'}})
  if(tabId==='home'){renderDayView()}else if(tabId==='month'){renderMonthView()}else if(tabId==='good-days'){filterGoodDays(currentGoodDayCategory);renderLottery()}else if(tabId==='prayers'){renderPrayersList()}
  if(isBellEnabled)playBell()
}
function showElderAlert(msg){document.getElementById('elder-alert-text').innerText=msg;document.getElementById('elder-alert-box').classList.add('open');if(isBellEnabled)playBell()}
function closeElderAlert(){document.getElementById('elder-alert-box').classList.remove('open')}

// ===== BRIDGE INTEGRATION =====
function updateLunarData(jsonStr){
  try{
    var data=JSON.parse(jsonStr)
    if(data&&data.year&&data.day){
      var m=data.monthYear?parseInt(data.monthYear.replace('Tháng ','')):(new Date().getMonth()+1)
      currentDate=data.year+'-'+padNumber(m)+'-'+padNumber(data.day)
      currentMonthView=m;currentYearView=data.year
    }
  }catch(e){}
  document.getElementById('app-loading').style.display='none'
  document.getElementById('app-content').style.display='block'
  renderDayView()
}
function refreshForClockChange(){
  var now=new Date(),today=localDateString(now),previousParts=lastKnownToday.split('-')
  var followedToday=currentDate===lastKnownToday
  var followedCurrentMonth=currentMonthView===parseInt(previousParts[1],10)&&currentYearView===parseInt(previousParts[0],10)
  lastKnownToday=today
  if(followedToday)currentDate=today
  if(followedToday&&followedCurrentMonth){currentMonthView=now.getMonth()+1;currentYearView=now.getFullYear()}
  if(currentTab==='home')renderDayView()
  else if(currentTab==='month')renderMonthView()
  else if(currentTab==='good-days'){
    filterGoodDays(currentGoodDayCategory)
    renderLottery()
  }
}
function scheduleClockRefresh(){
  clearTimeout(timeRefreshTimer)
  var now=new Date(),nextHour=new Date(now.getTime())
  nextHour.setHours(now.getHours()+1,0,1,0)
  timeRefreshTimer=setTimeout(function(){refreshForClockChange();scheduleClockRefresh()},Math.max(1000,nextHour.getTime()-now.getTime()))
}
window.onNativeResume=function(){refreshForClockChange();scheduleClockRefresh()}
// ===== INIT =====
window.onload=function(){
  renderDayView()
  renderPrayersList()
  scheduleClockRefresh()
  setTimeout(function(){
    document.getElementById('app-loading').style.display='none'
    document.getElementById('app-content').style.display='block'
  },200)
}
