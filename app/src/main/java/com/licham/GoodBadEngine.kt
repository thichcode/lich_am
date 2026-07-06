package com.licham

import org.json.JSONObject

data class HourInfo(
    val chiName: String,
    val timeRange: String,
    val isGood: Boolean,
    val reason: String = ""
)

data class DayAssessment(
    val isGood: Boolean,
    val score: Int,
    val label: String,
    val goodActivities: List<String>,
    val badActivities: List<String>,
    val goodHours: List<HourInfo>,
    val badHours: List<HourInfo>,
    val truc: String,
    val tu: String,
    val isHoangDao: Boolean
)

object GoodBadEngine {
    private val chi = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val trucNames = arrayOf("Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế")
    private val tuNames = arrayOf(
        "Giác", "Cang", "Đê", "Phòng", "Tâm", "Vĩ", "Cơ",
        "Đẩu", "Ngưu", "Nữ", "Hư", "Nguy", "Thất", "Bích",
        "Khuê", "Lâu", "Vị", "Mão", "Tất", "Chủy", "Sâm",
        "Tỉnh", "Quỷ", "Liễu", "Tinh", "Trương", "Dực", "Chẩn"
    )
    private val goodTuIndices = setOf(3, 4, 5, 7, 10, 12, 14, 15, 16, 17, 18, 19, 21, 22, 25, 26)
    private val badTuIndices = setOf(0, 1, 2, 6, 8, 9, 11, 13, 20, 23, 24, 27)

    private val hourTimeRanges = arrayOf(
        "23:00–00:59", "01:00–02:59", "03:00–04:59", "05:00–06:59",
        "07:00–08:59", "09:00–10:59", "11:00–12:59", "13:00–14:59",
        "15:00–16:59", "17:00–18:59", "19:00–20:59", "21:00–22:59"
    )

    private val hoangDaoHours = arrayOf(
        intArrayOf(1, 3, 5, 7, 9, 11),
        intArrayOf(2, 4, 6, 8, 10, 0),
        intArrayOf(3, 5, 7, 9, 11, 1),
        intArrayOf(4, 6, 8, 10, 0, 2),
        intArrayOf(5, 7, 9, 11, 1, 3),
        intArrayOf(6, 8, 10, 0, 2, 4),
        intArrayOf(7, 9, 11, 1, 3, 5),
        intArrayOf(8, 10, 0, 2, 4, 6),
        intArrayOf(9, 11, 1, 3, 5, 7),
        intArrayOf(10, 0, 2, 4, 6, 8),
        intArrayOf(11, 1, 3, 5, 7, 9),
        intArrayOf(0, 2, 4, 6, 8, 10)
    )

    private val tamNuongDays = setOf(3, 7, 13, 18, 22, 27)
    private val nguyetKyDays = setOf(5, 14, 23)
    private val duongCongKy = setOf(
        13 to 1, 11 to 2, 9 to 3, 7 to 4, 5 to 5, 3 to 6,
        1 to 7, 29 to 7, 27 to 8, 25 to 9, 23 to 10, 21 to 11, 19 to 12
    )

    private var rules = mutableMapOf<String, Int>()
    private var goodDefault = listOf("Cúng lễ", "Gặp gỡ", "Xuất hành")
    private var badDefault = listOf("Động thổ", "Cưới hỏi", "Tranh cãi")

    fun loadRules(jsonString: String?) {
        if (jsonString == null) return
        try {
            val root = JSONObject(jsonString)
            val scores = root.getJSONObject("score")
            rules.clear()
            for (key in scores.keys()) {
                rules[key] = scores.getInt(key)
            }
            val acts = root.getJSONObject("activities")
            val ga = acts.getJSONArray("good_default")
            goodDefault = (0 until ga.length()).map { ga.getString(it) }
            val ba = acts.getJSONArray("bad_default")
            badDefault = (0 until ba.length()).map { ba.getString(it) }
        } catch (_: Exception) {}
    }

    fun assessDay(
        lunarDay: Int,
        lunarMonth: Int,
        dayCanIndex: Int,
        dayChiIndex: Int,
        jd: Long,
        solarMonth: Int
    ): DayAssessment {
        val monthBranch = solarMonth % 12
        val trucIdx = ((dayChiIndex - monthBranch + 12) % 12).toInt()
        val trucName = trucNames[trucIdx]
        val hoangDaoDay = trucIdx in listOf(2, 3, 4, 5, 8, 10)

        val tuIdx = ((jd - 2449747) % 28 + 28).toInt() % 28
        val tuName = tuNames[tuIdx]

        val satChu = isSatChu(dayChiIndex, solarMonth)
        val tamNuong = lunarDay in tamNuongDays
        val nguyetKy = lunarDay in nguyetKyDays
        val duongCong = (lunarDay to lunarMonth) in duongCongKy

        var score = 0
        score += if (hoangDaoDay) (rules["hoang_dao"] ?: 20) else (rules["hac_dao"] ?: -20)
        if (trucIdx in listOf(2, 3, 4, 5, 8, 10)) score += (rules["truc_tot"] ?: 10)
        if (trucIdx in listOf(0, 1, 6, 7, 9, 11)) score += (rules["truc_xau"] ?: -10)
        if (tuIdx in goodTuIndices) score += (rules["tu_tot"] ?: 5)
        if (tuIdx in badTuIndices) score += (rules["tu_xau"] ?: -5)
        if (dayCanIndex % 2 == 0) score += 3
        if (dayCanIndex % 2 == 1) score -= 2
        if (tamNuong) score += (rules["tam_nuong"] ?: -30)
        if (nguyetKy) score += (rules["nguyet_ky"] ?: -25)
        if (satChu) score += (rules["sat_chu"] ?: -40)
        if (duongCong) score += (rules["duong_cong_ky"] ?: -35)

        val fullMoonBonus = if (lunarDay == 15) 10 else 0
        val firstDayBonus = if (lunarDay == 1) 8 else 0
        score += fullMoonBonus + firstDayBonus

        val isTet = lunarDay == 1 && lunarMonth == 1
        val isGood = score > 0 || isTet

        val label = when {
            isTet -> "Tết Nguyên Đán"
            score >= 30 -> "Đại Cát"
            score > 0 -> "Ngày Tốt"
            score == 0 -> "Ngày Bình Thường"
            score >= -30 -> "Ngày Xấu"
            else -> "Đại Hung"
        }

        val goodActs = if (score > 0) goodDefault.take(3) else emptyList()
        val badActs = if (score < 0) badDefault.take(3) else emptyList()

        val gh = hoangDaoHours[dayChiIndex]
        val goodHoursList = gh.map { h ->
            HourInfo(chiName = chi[h], timeRange = hourTimeRanges[h], isGood = true, reason = "Hoàng Đạo")
        }

        val allHours = (0..11).toSet()
        val badHoursList = (allHours - gh.toSet()).sorted().map { h ->
            HourInfo(chiName = chi[h], timeRange = hourTimeRanges[h], isGood = false, reason = "Hắc Đạo")
        }

        return DayAssessment(
            isGood = isGood,
            score = score,
            label = label,
            goodActivities = goodActs,
            badActivities = badActs,
            goodHours = goodHoursList,
            badHours = badHoursList,
            truc = trucName,
            tu = tuName,
            isHoangDao = hoangDaoDay
        )
    }

    private fun isSatChu(dayChi: Int, solarMonth: Int): Boolean {
        val season = when (solarMonth) {
            2, 3, 4 -> setOf(5, 9, 1)
            5, 6, 7 -> setOf(2, 6, 10)
            8, 9, 10 -> setOf(11, 4, 7)
            else -> setOf(8, 0, 4)
        }
        return dayChi in season
    }
}
