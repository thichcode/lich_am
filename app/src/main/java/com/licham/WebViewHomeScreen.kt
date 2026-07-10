package com.licham

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.time.LocalDate
import java.util.Calendar

private class LunarJsBridge(
    private val webView: WebView,
    private val context: Context
) {
    private var currentDate = LocalDate.now()

    @JavascriptInterface
    fun requestInitialData() {
        val data = buildLunarData(currentDate)
        postData(data)
    }

    @JavascriptInterface
    fun changeDate(offset: Int) {
        currentDate = if (offset == 0) LocalDate.now() else currentDate.plusDays(offset.toLong())
        val data = buildLunarData(currentDate)
        postData(data)
    }

    private fun buildLunarData(date: LocalDate): String {
        try {
            val lunar = LunarCalculator.solar2lunar(date.dayOfMonth, date.monthValue, date.year)
            val jd = LunarCalculator.jdFromDate(date.dayOfMonth, date.monthValue, date.year)
            val dayCanChi = CanChiCalculator.getDayCanChi(jd)
            val yearCanChi = CanChiCalculator.getYearCanChi(date.year)
            val yearCanIndex = CanChiCalculator.getYearCanIndex(date.year)
            val monthCanChi = if (lunar != null) CanChiCalculator.getMonthCanChi(lunar.month, yearCanIndex) else null

            val canIndex = when (dayCanChi.first) {
                "Giáp" -> 0; "Ất" -> 1; "Bính" -> 2; "Đinh" -> 3; "Mậu" -> 4
                "Kỷ" -> 5; "Canh" -> 6; "Tân" -> 7; "Nhâm" -> 8; "Quý" -> 9
                else -> 0
            }
            val chiIndex = when (dayCanChi.second) {
                "Tý" -> 0; "Sửu" -> 1; "Dần" -> 2; "Mão" -> 3; "Thìn" -> 4; "Tỵ" -> 5
                "Ngọ" -> 6; "Mùi" -> 7; "Thân" -> 8; "Dậu" -> 9; "Tuất" -> 10; "Hợi" -> 11
                else -> 0
            }

            val assessment = if (lunar != null) {
                GoodBadEngine.assessDay(lunar.day, lunar.month, canIndex, chiIndex, jd, date.monthValue)
            } else null

            val terms = TietKhiCalculator.getCurrentAndNext(date)
            val events = if (lunar != null) {
                EventProvider.getTodayEvents(date.dayOfMonth, date.monthValue, lunar.day, lunar.month)
            } else emptyList()
            val quote = QuoteProvider.getRandomQuote().text

            val weekdayNames = arrayOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
            val weekday = weekdayNames[date.dayOfWeek.value % 7]

            val chiNames = arrayOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi")
            val animalNames = arrayOf("Chuột","Trâu","Hổ","Mèo","Rồng","Rắn","Ngựa","Dê","Khỉ","Gà","Chó","Lợn")
            val lunarMonthNames = arrayOf("", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp")
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val currentHourChi = if (lunar != null) {
                CanChiCalculator.getHourChiName(currentHour)
            } else ""

            val hourMap = mapOf(
                0 to "Tý", 1 to "Sửu", 2 to "Dần", 3 to "Mão",
                4 to "Thìn", 5 to "Tỵ", 6 to "Ngọ", 7 to "Mùi",
                8 to "Thân", 9 to "Dậu", 10 to "Tuất", 11 to "Hợi"
            )
            val timeRanges = arrayOf(
                "23:00–00:59","01:00–02:59","03:00–04:59","05:00–06:59",
                "07:00–08:59","09:00–10:59","11:00–12:59","13:00–14:59",
                "15:00–16:59","17:00–18:59","19:00–20:59","21:00–22:59"
            )

            val goodHoursArr = assessment?.goodHours?.take(6)?.map { h ->
                val idx = hourMap.entries.firstOrNull { it.value == h.chiName }?.key ?: 0
                """{"chi":"${h.chiName}","time":"${timeRanges.getOrElse(idx) { "??" }}"}"""
            } ?: emptyList()

            val badHoursArr = assessment?.badHours?.take(6)?.map { h ->
                val idx = hourMap.entries.firstOrNull { it.value == h.chiName }?.key ?: 0
                """{"chi":"${h.chiName}","time":"${timeRanges.getOrElse(idx) { "??" }}"}"""
            } ?: emptyList()

            val clashAges: String
            val hyThan: String
            val taiThan: String

            if (assessment != null) {
                val conflictMap = mapOf(
                    "Tý" to "Mậu Ngọ, Nhâm Ngọ, Canh Tý",
                    "Sửu" to "Kỷ Mùi, Quý Mùi, Tân Sửu",
                    "Dần" to "Canh Thân, Giáp Thân, Mậu Dần",
                    "Mão" to "Tân Dậu, Ất Dậu, Kỷ Mão",
                    "Thìn" to "Nhâm Tuất, Bính Tuất, Giáp Thìn",
                    "Tỵ" to "Quý Hợi, Đinh Hợi, Ất Tỵ",
                    "Ngọ" to "Nhâm Tý, Bính Tý, Giáp Ngọ",
                    "Mùi" to "Quý Sửu, Đinh Sửu, Ất Mùi",
                    "Thân" to "Mậu Dần, Bính Dần, Canh Thân",
                    "Dậu" to "Kỷ Mão, Đinh Mão, Tân Dậu",
                    "Tuất" to "Canh Thìn, Bính Thìn, Mậu Tuất",
                    "Hợi" to "Tân Tỵ, Đinh Tỵ, Kỷ Hợi"
                )
                clashAges = conflictMap[chiNames[chiIndex]] ?: ""

                hyThan = when (canIndex) { 0,1 -> "Đông Bắc"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }
                taiThan = when (canIndex) { 0,1 -> "Đông Nam"; 2,3 -> "Chính Tây"; 4,5 -> "Chính Đông"; 6,7 -> "Chính Nam"; else -> "Tây Bắc" }
            } else {
                clashAges = ""
                hyThan = ""
                taiThan = ""
            }

            val gActivities = assessment?.goodActivities?.joinToString(", ") ?: ""
            val bActivities = assessment?.badActivities?.joinToString(", ") ?: ""
            val gFallback = if (assessment != null) GoodBadEngine.getTrucGoodActivities(assessment.trucIdx).joinToString(", ") else ""
            val bFallback = if (assessment != null) GoodBadEngine.getTrucBadActivities(assessment.trucIdx).joinToString(", ") else ""

            val eventsJson = events.mapIndexed { index, event ->
                val dateText = if (index == 0) {
                    "${date.dayOfMonth.toString().padStart(2,'0')}/${date.monthValue.toString().padStart(2,'0')}/${date.year}"
                } else {
                    val lunarText = if (lunar != null) " (${lunar.day.toString().padStart(2,'0')}/${lunar.month.toString().padStart(2,'0')} ÂL)" else ""
                    "${date.dayOfMonth.toString().padStart(2,'0')}/${date.monthValue.toString().padStart(2,'0')}/${date.year}$lunarText"
                }
                """{"date":"$dateText","name":"$event"}"""
            }

            val monthChiIdx = monthCanChi?.let { chiNames.indexOf(it.second) } ?: 0
            val yearChiIdx = chiNames.indexOf(yearCanChi.second)

            val sb = StringBuilder()
            sb.append("{")
            sb.append("\"year\":${date.year},")
            sb.append("\"weekday\":\"$weekday\",")
            sb.append("\"day\":${date.dayOfMonth},")
            sb.append("\"monthYear\":\"Tháng ${date.monthValue} ${date.year}\",")
            sb.append("\"quote\":\"${quote.replace("\"", "\\\"").replace("\n", " ")}\",")

            if (lunar != null) {
                sb.append("\"lunar\":{")
                sb.append("\"day\":${lunar.day},")
                sb.append("\"month\":${lunar.month},")
                sb.append("\"year\":${lunar.year},")
                sb.append("\"monthName\":\"${lunarMonthNames.getOrElse(lunar.month) { lunar.month.toString() }}\"")
                sb.append("},")
                sb.append("\"currentHourChi\":\"$currentHourChi\",")
                sb.append("\"dayCanChi\":\"${dayCanChi.first} ${dayCanChi.second}\",")
                sb.append("\"dayAnimal\":\"${animalNames.getOrElse(chiIndex) { "" }}\",")
                sb.append("\"monthCanChi\":\"${monthCanChi?.let { "${it.first} ${it.second}" } ?: ""}\",")
                sb.append("\"monthAnimal\":\"${animalNames.getOrElse(monthChiIdx) { "" }}\",")
                sb.append("\"yearCanChi\":\"${yearCanChi.first} ${yearCanChi.second}\",")
                sb.append("\"yearAnimal\":\"${animalNames.getOrElse(yearChiIdx) { "" }}\"")
            } else {
                sb.append("\"lunar\":null,")
                sb.append("\"currentHourChi\":\"\",")
                sb.append("\"dayCanChi\":\"\",\"dayAnimal\":\"\",")
                sb.append("\"monthCanChi\":\"\",\"monthAnimal\":\"\",")
                sb.append("\"yearCanChi\":\"\",\"yearAnimal\":\"\"")
            }

            if (assessment != null) {
                sb.append(",\"goodHours\":[${goodHoursArr.joinToString(",")}],")
                sb.append("\"badHours\":[${badHoursArr.joinToString(",")}],")
                sb.append("\"goodActivities\":\"${gActivities.ifBlank { gFallback }}\",")
                sb.append("\"badActivities\":\"${bActivities.ifBlank { bFallback }}\",")
                sb.append("\"clashAges\":\"$clashAges\",")
                sb.append("\"hyThan\":\"$hyThan\",")
                sb.append("\"taiThan\":\"$taiThan\"")
            } else {
                sb.append(",\"goodHours\":[],\"badHours\":[],")
                sb.append("\"goodActivities\":\"\",\"badActivities\":\"\",")
                sb.append("\"clashAges\":\"\",\"hyThan\":\"\",\"taiThan\":\"\"")
            }

            if (terms != null) {
                sb.append(",\"termCurrent\":{\"name\":\"${terms.first.name}\",\"date\":\"${terms.first.date.dayOfMonth.toString().padStart(2,'0')}/${terms.first.date.monthValue.toString().padStart(2,'0')}/${terms.first.date.year}\"},")
                sb.append("\"termNext\":{\"name\":\"${terms.second.name}\",\"date\":\"${terms.second.date.dayOfMonth.toString().padStart(2,'0')}/${terms.second.date.monthValue.toString().padStart(2,'0')}/${terms.second.date.year}\"}")
            } else {
                sb.append(",\"termCurrent\":null,\"termNext\":null")
            }

            sb.append(",\"events\":[${eventsJson.joinToString(",")}]")
            sb.append("}")

            return sb.toString()
        } catch (e: Exception) {
            return """{"error":"${e.message}"}"""
        }
    }

    private fun postData(json: String) {
        webView.post {
            webView.evaluateJavascript("updateLunarData('${json.replace("'", "\\'").replace("\n", " ")}')", null)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewHomeScreen(useCompose: Boolean, @Suppress("UNUSED_PARAMETER") onSwitchToCompose: () -> Unit = {}) {
    if (useCompose) {
        HomeScreen()
        return
    }

    val context = LocalContext.current
    val uiManager = remember { UiPackageManager.getInstance(context) }
    var wv by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        // Pre-calculate and push initial data after WebView loads
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = false
                settings.setGeolocationEnabled(false)

                val packagePath = uiManager.getActivePackagePath()
                val url = if (packagePath.startsWith("/")) {
                    "file://$packagePath/index.html"
                } else {
                    "file:///android_asset/$packagePath/index.html"
                }

                addJavascriptInterface(LunarJsBridge(this, ctx), "nativeApp")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "if(typeof nativeApp !== 'undefined' && nativeApp.requestInitialData) { nativeApp.requestInitialData(); }",
                            null
                        )
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        val scheme = uri.scheme ?: ""
                        val host = uri.host ?: ""
                        // Allow local files and the update server
                        if (scheme == "file") return false
                        if (host == "lich-am-1.vercel.app") return false
                        return true
                    }
                }

                webChromeClient = WebChromeClient()

                loadUrl(url)
                wv = this
            }
        },
        update = { }
    )

    DisposableEffect(Unit) {
        onDispose {
            wv?.destroy()
        }
    }
}
