// ===== LUNAR CALCULATION ENGINE =====
function INT(x){return Math.floor(x)}
function jdFromDate(dd,mm,yy){
  var a=INT((14-mm)/12),y=yy+4800-a,m=mm+12*a-3,jd=dd+INT((153*m+2)/5)+365*y+INT(y/4)-INT(y/100)+INT(y/400)-32045
  if(jd<2299161)jd=dd+INT((153*m+2)/5)+365*y+INT(y/4)-32083
  return jd
}
function jdToDate(jd){
  var a,b,c,d,e,m,day,month,year
  if(jd>2299160){a=jd+32044;b=INT((4*a+3)/146097);c=a-INT((b*146097)/4)}else{b=0;c=jd+32082}
  d=INT((4*c+3)/1461);e=c-INT((1461*d)/4);m=INT((5*e+2)/153)
  day=e-INT((153*m+2)/5)+1;month=m+3-12*INT(m/10);year=b*100+d-4800+INT(m/10)
  return[day,month,year]
}
function getNewMoonDay(k,timeZone){
  var T=k/1236.85,T2=T*T,T3=T2*T,dr=Math.PI/180
  var Jd1=2415020.75933+29.53058868*k+0.0001178*T2-0.000000155*T3
  Jd1+=0.00033*Math.sin((166.56+132.87*T-0.009173*T2)*dr)
  var M=359.2242+29.10535608*k-0.0000333*T2-0.00000347*T3
  var Mpr=306.0253+385.81691806*k+0.0107306*T2+0.00001236*T3
  var F=21.2964+390.67050646*k-0.0016528*T2-0.00000236*T3
  var C1=(0.1734-0.000393*T)*Math.sin(M*dr)+0.0021*Math.sin(2*dr*M)
  C1-=0.4068*Math.sin(Mpr*dr)+0.0161*Math.sin(dr*2*Mpr)
  C1-=0.0004*Math.sin(dr*3*Mpr)+0.0104*Math.sin(dr*2*F)-0.0051*Math.sin(dr*(M+Mpr))
  C1-=0.0074*Math.sin(dr*(M-Mpr))+0.0004*Math.sin(dr*(2*F+M))
  C1-=0.0004*Math.sin(dr*(2*F-M))-0.0006*Math.sin(dr*(2*F+Mpr))
  C1+=0.0010*Math.sin(dr*(2*F-Mpr))+0.0005*Math.sin(dr*(2*Mpr+M))
  var deltat=T<-11?0.001+0.000839*T+0.0002261*T2-0.00000845*T3-0.000000081*T*T3:-0.000278+0.000265*T+0.000262*T2
  return INT(Jd1+C1-deltat+0.5+timeZone/24)
}
function getSunLongitude(jdn,timeZone){
  var T=(jdn-2451545.5-timeZone/24)/36525,T2=T*T,dr=Math.PI/180
  var M=357.52910+35999.05030*T-0.0001559*T2-0.00000048*T*T2
  var L0=280.46645+36000.76983*T+0.0003032*T2
  var DL=(1.91460-0.004817*T-0.000014*T2)*Math.sin(dr*M)+(0.019993-0.000101*T)*Math.sin(dr*2*M)+0.000290*Math.sin(dr*3*M)
  var L=(L0+DL)*dr;L-=Math.PI*2*INT(L/(Math.PI*2))
  if(L<0)L+=Math.PI*2
  return INT(L/Math.PI*6)
}
function getLunarMonth11(yy,timeZone){
  var off=jdFromDate(31,12,yy)-2415021,k=INT(off/29.530588853),nm=getNewMoonDay(k,timeZone)
  if(getSunLongitude(nm,timeZone)>=9)nm=getNewMoonDay(k-1,timeZone)
  return nm
}
function getLeapMonthOffset(a11,timeZone){
  var k=INT((a11-2415021.076998695)/29.530588853+0.5),last=0,i=1,arc=getSunLongitude(getNewMoonDay(k+i,timeZone),timeZone)
  do{last=arc;i++;arc=getSunLongitude(getNewMoonDay(k+i,timeZone),timeZone)}while(arc!=last&&i<14)
  return i-1
}
function convertSolar2Lunar(dd,mm,yy,timeZone){
  var dayNumber=jdFromDate(dd,mm,yy),k=INT((dayNumber-2415021.076998695)/29.530588853),monthStart=getNewMoonDay(k+1,timeZone)
  if(monthStart>dayNumber)monthStart=getNewMoonDay(k,timeZone)
  var a11=getLunarMonth11(yy,timeZone),b11=a11,lunarYear
  if(a11>=monthStart){lunarYear=yy;a11=getLunarMonth11(yy-1,timeZone)}else{lunarYear=yy+1;b11=getLunarMonth11(yy+1,timeZone)}
  var lunarDay=dayNumber-monthStart+1,diff=INT((monthStart-a11)/29),lunarLeap=0,lunarMonth=diff+11
  if(b11-a11>365){var leapMonthDiff=getLeapMonthOffset(a11,timeZone);if(diff>=leapMonthDiff){lunarMonth=diff+10;if(diff==leapMonthDiff)lunarLeap=1}}
  if(lunarMonth>12)lunarMonth-=12
  if(lunarMonth>=11&&diff<4)lunarYear-=1
  return[lunarDay,lunarMonth,lunarYear,lunarLeap]
}
var CANS=['Giáp','Ất','Bính','Đinh','Mậu','Kỷ','Canh','Tân','Nhâm','Quý']
var CHIS=['Tý','Sửu','Dần','Mão','Thìn','Tỵ','Ngọ','Mùi','Thân','Dậu','Tuất','Hợi']
var ANIMALS=['Chuột','Trâu','Hổ','Mèo','Rồng','Rắn','Ngựa','Dê','Khỉ','Gà','Chó','Lợn']
function getYearCanChi(lunarYear){
  var ci=(lunarYear+6)%10,zi=(lunarYear+8)%12
  return{text:CANS[ci]+' '+CHIS[zi],animal:ANIMALS[zi]}
}
function getMonthCanChi(lunarYear,lunarMonth){
  var yci=(lunarYear+6)%10,ci=(yci*2+lunarMonth+1)%10,zi=(lunarMonth+1)%12
  return{text:CANS[ci]+' '+CHIS[zi],animal:ANIMALS[zi]}
}
function getDayCanChi(jd){
  var ci=(jd+9)%10,zi=(jd+1)%12
  return{text:CANS[ci]+' '+CHIS[zi],animal:ANIMALS[zi]}
}
function getHoursForDay(chiIdx){
  var good=[]
  if(chiIdx===0||chiIdx===6)good=[0,1,3,6,8,9]
  else if(chiIdx===1||chiIdx===7)good=[2,3,5,8,10,11]
  else if(chiIdx===2||chiIdx===8)good=[0,1,4,5,7,10]
  else if(chiIdx===3||chiIdx===9)good=[0,2,3,6,7,9]
  else if(chiIdx===4||chiIdx===10)good=[2,4,5,8,9,11]
  else if(chiIdx===5||chiIdx===11)good=[1,4,6,7,10,11]
  var allH=[{t:'23h-01h',l:'Tý'},{t:'01h-03h',l:'Sửu'},{t:'03h-05h',l:'Dần'},{t:'05h-07h',l:'Mão'},{t:'07h-09h',l:'Thìn'},{t:'09h-11h',l:'Tỵ'},{t:'11h-13h',l:'Ngọ'},{t:'13h-15h',l:'Mùi'},{t:'15h-17h',l:'Thân'},{t:'17h-19h',l:'Dậu'},{t:'19h-21h',l:'Tuất'},{t:'21h-23h',l:'Hợi'}]
  var gh=[],bh=[]
  for(var i=0;i<12;i++){if(good.includes(i))gh.push(allH[i]);else bh.push(allH[i])}
  return{goodHours:gh,badHours:bh}
}
function getSolarTerm(day,month){
  var terms=[{n:'Tiểu Hàn',m:1,d:5},{n:'Đại Hàn',m:1,d:20},{n:'Lập Xuân',m:2,d:4},{n:'Vũ Thủy',m:2,d:19},{n:'Kinh Trập',m:3,d:5},{n:'Xuân Phân',m:3,d:20},{n:'Thanh Minh',m:4,d:4},{n:'Cốc Vũ',m:4,d:20},{n:'Lập Hạ',m:5,d:5},{n:'Tiểu Mãn',m:5,d:21},{n:'Mang Chủng',m:6,d:5},{n:'Hạ Chí',m:6,d:21},{n:'Tiểu Thử',m:7,d:7},{n:'Đại Thử',m:7,d:22},{n:'Lập Thu',m:8,d:7},{n:'Xử Thử',m:8,d:23},{n:'Bạch Lộ',m:9,d:7},{n:'Thu Phân',m:9,d:23},{n:'Hàn Lộ',m:10,d:8},{n:'Sương Giáng',m:10,d:23},{n:'Lập Đông',m:11,d:7},{n:'Tiểu Tuyết',m:11,d:22},{n:'Đại Tuyết',m:12,d:7},{n:'Đông Chí',m:12,d:21}]
  var ci=-1;for(var i=0;i<terms.length;i++){if(month>terms[i].m||(month===terms[i].m&&day>=terms[i].d))ci=i}
  if(ci===-1)ci=terms.length-1
  var ni=(ci+1)%terms.length
  return{current:terms[ci].n,currentStart:String(terms[ci].d).padStart(2,'0')+'/'+String(terms[ci].m).padStart(2,'0'),next:terms[ni].n,nextStart:String(terms[ni].d).padStart(2,'0')+'/'+String(terms[ni].m).padStart(2,'0')}
}
function getElderDayData(dateStr){
  var d=new Date(dateStr);if(isNaN(d.getTime()))d=new Date()
  var day=d.getDate(),month=d.getMonth()+1,year=d.getFullYear()
  var wd=['CHỦ NHẬT','THỨ HAI','THỨ BA','THỨ TƯ','THỨ NĂM','THỨ SÁU','THỨ BẢY']
  var ld=convertSolar2Lunar(day,month,year,7)
  var ly=ld[0],lm=ld[1],lyr=ld[2],ll=ld[3],yc=getYearCanChi(lyr),mc=getMonthCanChi(lyr,lm),jd=jdFromDate(day,month,year),dc=getDayCanChi(jd)
  var isGood=(jd%2===0),quotes=['Một cây làm chẳng nên non ba cây chụm lại nên hòn núi cao.','Kính lão đắc thọ, trọng già già để tuổi cho.','Ăn quả nhớ kẻ trồng cây, ăn khoai nhớ kẻ cho dây mà trồng.','Con người có tổ có tông, như cây có cội như sông có nguồn.','Lá rụng về cội, nước chảy về nguồn.','Gieo hành vi gặt thói quen, gieo thói quen gặt tính cách.']
  var qi=(day+month)%quotes.length,dci=(jd+1)%12,hrs=getHoursForDay(dci)
  var cm={'Chuột':'Mậu Ngọ, Nhâm Ngọ, Canh Tý','Trâu':'Kỷ Mùi, Quý Mùi, Tân Sửu','Hổ':'Canh Thân, Giáp Thân, Mậu Dần','Mèo':'Tân Dậu, Ất Dậu, Kỷ Mão','Rồng':'Nhâm Tuất, Bính Tuất, Giáp Thìn','Rắn':'Quý Hợi, Đinh Hợi, Ất Tỵ','Ngựa':'Nhâm Tý, Bính Tý, Giáp Ngọ','Dê':'Quý Sửu, Đinh Sửu, Ất Mùi','Khỉ':'Mậu Dần, Bính Dần, Canh Thần','Gà':'Kỷ Mão, Đinh Mão, Tân Dậu','Chó':'Canh Thìn, Bính Thìn, Mậu Tuất','Hợi':'Tân Tỵ, Đinh Tỵ, Kỷ Hợi'}
  var ca=cm[dc.animal]||'Xung khắc các tuổi Thân, Dần',term=getSolarTerm(day,month)
  return{solar:{day:day,month:'Tháng '+month,year:year,weekday:wd[d.getDay()]},lunar:{day:ly,month:'Tháng '+(lm<10?'0':'')+lm+(ll?' (Nhuận)':''),yearChi:yc.text,monthChi:mc.text,dayChi:dc.text,yearAnimal:yc.animal,monthAnimal:mc.animal,dayAnimal:dc.animal},isGoodDay:isGood,starText:isGood?'Ngày Hoàng Đạo':'Ngày Hắc Đạo',quote:quotes[qi],goodHours:hrs.goodHours,badHours:hrs.badHours,shouldDo:isGood?'Cúng tế, làm từ thiện, dọn dẹp ban thờ':'Chăm sóc sức khỏe, nghỉ ngơi tĩnh dưỡng, uống trà thiền',shouldAvoid:isGood?'Mâu thuẫn cãi cọ, xuất hành đi quá xa':'Khai trương lớn, mua bán tài sản trọng đại, cưới hỏi',clashAges:ca,solarTerm:term.current,termStart:term.currentStart+'/'+year,nextTerm:term.next,nextTermStart:term.nextStart+'/'+year}
}
// ===== STATE =====
var initDate=new Date(),currentDate=initDate.getFullYear()+'-'+String(initDate.getMonth()+1).padStart(2,'0')+'-'+String(initDate.getDate()).padStart(2,'0'),currentTab='home',currentMonthView=initDate.getMonth()+1,currentYearView=initDate.getFullYear(),isBellEnabled=false,isHighContrast=false,isAutoScrolling=false,scrollTimer=null,currentPrayerFontSize=18
// ===== TOAST =====
function showElderToast(text){var t=document.getElementById('elder-toast'),tt=document.getElementById('elder-toast-text');if(!t||!tt)return;tt.innerText=text;t.classList.add('show');setTimeout(function(){t.classList.remove('show')},2000)}
// ===== CONTRAST =====
function toggleElderContrast(){isHighContrast=!isHighContrast;var ic=document.getElementById('contrastIcon'),lb=document.getElementById('contrast-label');if(isHighContrast){document.body.classList.add('dark');ic.innerText='☀️';lb.innerText='Giao diện tối đang bật';showElderToast('Đã bật giao diện dịu mắt')}else{document.body.classList.remove('dark');ic.innerText='🌓';lb.innerText='Giao diện tối dịu nhẹ';showElderToast('Đã về giao diện ấm áp')}}
// ===== NAVIGATION =====
function navigateDay(offset){
  var d=new Date(currentDate);d.setDate(d.getDate()+offset)
  currentDate=d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')
  currentMonthView=d.getMonth()+1;currentYearView=d.getFullYear()
  if(isBellEnabled)playBell()
  renderDayView()
  if(typeof nativeApp!=='undefined'&&nativeApp.changeDate)nativeApp.changeDate(offset)
  showElderToast('Đã chuyển tới ngày '+String(d.getDate()).padStart(2,'0')+'/'+String(d.getMonth()+1).padStart(2,'0'))
}
function resetToSampleDate(){
  var t=new Date()
  currentDate=t.getFullYear()+'-'+String(t.getMonth()+1).padStart(2,'0')+'-'+String(t.getDate()).padStart(2,'0')
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
  var now=new Date(),h=now.getHours(),hci=Math.floor(h/2)%12
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
  var today=new Date(),act=new Date(currentDate)
  for(var d=1;d<=td;d++){
    var ds=currentYearView+'-'+String(currentMonthView).padStart(2,'0')+'-'+String(d).padStart(2,'0'),dd=getElderDayData(ds)
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
  var cats=['cuoihoi','khaitruong','dongtho']
  cats.forEach(function(c){var b=document.getElementById('btn-f-'+c);if(c===type)b.style.cssText='flex:1;padding:10px 4px;border-radius:10px;font-size:clamp(11px,3vw,13px);font-weight:900;text-align:center;border:2px solid #B45309;background:#064E3B;color:#fff;cursor:pointer';else b.style.cssText='flex:1;padding:10px 4px;border-radius:10px;font-size:clamp(11px,3vw,13px);font-weight:900;text-align:center;border:2px solid transparent;background:#E5E7EB;color:#1F2937;cursor:pointer'})
  var list=document.getElementById('good-days-list')
  var samples=[{s:'15/07/2026',l:'01/06 âm',d:'Ngày Tân Tỵ - Cực Kỳ Đại Cát',sub:'Mưu cầu hạnh phúc gia đạo viên mãn'},{s:'22/07/2026',l:'08/06 âm',d:'Ngày Mậu Tý - Ngày Hoàng Đạo',sub:'Tốt động thổ, sửa sang mồ mả'},{s:'27/07/2026',l:'13/06 âm',d:'Ngày Quý Tỵ - Lộc Tài',sub:'Hợp khai trương, rước may mắn'}]
  list.innerHTML=samples.map(function(i){return'<div class="good-day-item"><div class="good-day-badge"><span class="good-day-num">'+i.s.split('/')[0]+'</span><span class="good-day-month">THÁNG '+i.s.split('/')[1]+'</span></div><div class="good-day-info"><div class="good-day-desc">'+i.d+'</div><div class="good-day-lunar">Âm: '+i.l+'</div><div class="good-day-sub">'+i.sub+'</div></div></div>'}).join('')
  renderLottery()
}
function renderLottery(){
  var d=new Date(),dd=String(d.getDate()).padStart(2,'0'),mm=String(d.getMonth()+1).padStart(2,'0'),yy=d.getFullYear()
  function rn(len){var s='';for(var i=0;i<len;i++)s+=Math.floor(Math.random()*10);return s}
  var regions=[
    {name:'Miền Bắc',badge:'MB',color:'#991B1B',prizes:[
      {label:'ĐB',num:rn(5),special:true},{label:'Nhất',num:rn(5),special:false},{label:'Nhì',num:rn(5)},{label:'Ba',num:rn(5)},{label:'Bảy',num:rn(3)}
    ]},
    {name:'Miền Trung',badge:'MT',color:'#064E3B',prizes:[
      {label:'ĐB',num:rn(6),special:true},{label:'Nhất',num:rn(5),special:false},{label:'Nhì',num:rn(5)},{label:'Tám',num:rn(2)}
    ]},
    {name:'Miền Nam',badge:'MN',color:'#B45309',prizes:[
      {label:'ĐB',num:rn(6),special:true},{label:'Nhất',num:rn(5),special:false},{label:'Nhì',num:rn(5)},{label:'Tám',num:rn(2)}
    ]}
  ]
  var html=regions.map(function(r){
    var ps=r.prizes.map(function(p){
      var cls='lottery-prize'+(p.special?' lottery-prize-special':'')
      return '<span class="'+cls+'"><span class="'+(r.name==='Miền Bắc'?'red':'green')+'">'+p.label+'</span> '+p.num+'</span>'
    }).join('')
    return '<div class="lottery-region"><div class="lottery-region-title"><span class="badge" style="background:'+r.color+'">'+r.badge+'</span> '+r.name+'</div><div class="lottery-prizes">'+ps+'</div></div>'
  }).join('')
  html+='<div style="text-align:center;font-size:clamp(8px,2.2vw,10px);color:#9CA3AF;font-weight:700;margin-top:4px">KQXS '+dd+'/'+mm+'/'+yy+' — Dữ liệu mô phỏng</div>'
  document.getElementById('lottery-body').innerHTML=html
}
// ===== NEWS =====
function renderNews(){
  var d=new Date(),cats=[{name:'Kinh tế',color:'#064E3B',icon:'💰'},{name:'Chính trị',color:'#991B1B',icon:'🏛️'},{name:'Thể thao',color:'#2563EB',icon:'⚽'}]
  var articles=[]
  for(var i=0;i<6;i++){
    var c=cats[i%3],a={cat:c.name,color:c.color,icon:c.icon,title:'',source:'',link:'#'}
    if(c.name==='Kinh tế'){var ts=['Giá xăng dầu hôm nay: Xăng RON 95 giảm xuống còn 23.500 đồng/lít','Chứng khoán VN-Index vượt mốc 1.300 điểm, thanh khoản cao','Ngân hàng Nhà nước điều chỉnh lãi suất điều hành từ tháng sau','Đề án đường sắt cao tốc Bắc-Nam trình Quốc hội','Xuất khẩu gạo Việt Nam đạt kỷ lục 5 tỷ USD','Giá vàng trong nước đi ngang, quanh mốc 85 triệu đồng/lượng']
      a.title=ts[i%6];a.source=['VnExpress','Tuổi Trẻ','Thanh Niên','VietnamNet','CafeF','Đầu Tư'][i%6]
    }else if(c.name==='Chính trị'){var ts=['Quốc hội thông qua Luật Đất đai sửa đổi với nhiều điểm mới','Thủ tướng chỉ đạo đẩy nhanh tiến độ các dự án trọng điểm','Hội nghị Trung ương bàn về phát triển kinh tế xã hội','Việt Nam đảm nhận vai trò Ủy viên không thường trực HĐBA Liên Hợp Quốc','Cải cách hành chính: giảm 30% thủ tục giấy tờ','Tăng cường hợp tác Việt Nam - Hoa Kỳ trong lĩnh vực công nghệ']
      a.title=ts[i%6];a.source=['VnExpress','Tuổi Trẻ','Báo Chính Phủ','VietnamPlus','Nhân Dân','Dân Trí'][i%6]
    }else{var ts=['Đội tuyển Việt Nam thắng đậm 3-0 trước Indonesia tại AFF Cup','VFF bổ nhiệm huấn luyện viên mới cho đội tuyển quốc gia','VĐV Nguyễn Thị Ánh Viên giành HCV SEA Games','U23 Việt Nam vào bán kết giải U23 châu Á','Lịch thi đấu vòng loại World Cup 2026 của đội tuyển Việt Nam','Giải bóng đá V-League mùa giải mới khai mạc với nhiều bất ngờ']
      a.title=ts[i%6];a.source=['VnExpress','Tuổi Trẻ','Bóng Đá','Thể Thao 247','Tổng cục TDTT','Sài Gòn Giải Phóng'][i%6]
    }
    articles.push(a)
  }
  var colors=['#D1FAE5','#FEE2E2','#DBEAFE']
  var html=articles.map(function(a,i){
    var msg=a.title+' (Nguồn: '+a.source+') - Liên kết đọc sẽ mở trong ứng dụng thật.'
    return '<div class="news-article" onclick="showElderAlert(\''+msg.replace(/'/g,"\\'")+'\')"><div class="news-article-thumb" style="background:'+colors[i%3]+'">'+a.icon+'</div><div class="news-article-info"><div class="news-article-title">'+a.title+'</div><div class="news-article-source"><span class="news-article-cat" style="background:'+a.color+'">'+a.cat+'</span><span>'+a.source+'</span><span style="color:#064E3B;font-weight:700">🔗 Đọc tiếp</span></div></div></div>'
  }).join('')
  document.getElementById('news-list').innerHTML=html
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
  tabs.forEach(function(t){var el=document.getElementById('tab-'+t);if(t===tabId){el.classList.remove('hidden');el.classList.add('tab-section')}else{el.classList.add('hidden');el.classList.remove('tab-section')}})
  tabs.forEach(function(t){var btn=document.getElementById('nav-'+t),icon=btn.querySelector('.nav-icon'),label=btn.querySelector('.nav-label');if(t===tabId){icon.className='nav-icon nav-icon-active';label.style.color='#064E3B'}else{icon.className='nav-icon nav-icon-inactive';label.style.color='#78716C'}})
  if(tabId==='home'){renderDayView()}else if(tabId==='month'){renderMonthView()}else if(tabId==='good-days'){filterGoodDays('cuoihoi');renderLottery()}else if(tabId==='prayers'){renderPrayersList()}
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
      currentDate=data.year+'-'+String(m).padStart(2,'0')+'-'+String(data.day).padStart(2,'0')
      currentMonthView=m;currentYearView=data.year
    }
  }catch(e){}
  document.getElementById('app-loading').style.display='none'
  document.getElementById('app-content').style.display='block'
  renderDayView()
}
// ===== INIT =====
window.onload=function(){
  renderDayView()
  renderPrayersList()
  setTimeout(function(){
    document.getElementById('app-loading').style.display='none'
    document.getElementById('app-content').style.display='block'
  },200)
}
