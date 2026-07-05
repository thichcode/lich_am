package com.licham

import org.json.JSONArray
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
    val badHours: List<HourInfo>
)

object GoodBadEngine {
    private val chi = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    private val hourTimeRanges = arrayOf(
        "23:00 - 00:59", "01:00 - 02:59", "03:00 - 04:59", "05:00 - 06:59",
        "07:00 - 08:59", "09:00 - 10:59", "11:00 - 12:59", "13:00 - 14:59",
        "15:00 - 16:59", "17:00 - 18:59", "19:00 - 20:59", "21:00 - 22:59"
    )

    private val defaultGoodActivities = listOf(
        "Cầu tài lộc", "Xuất hành", "Khai trương", "Cưới hỏi",
        "Nhập trạch", "Động thổ", "Xây dựng", "Mua xe",
        "Ký kết hợp đồng", "Du lịch", "Gặp gỡ đối tác",
        "Bắt đầu công việc mới"
    )

    private val defaultBadActivities = listOf(
        "Chôn cất", "Phá thổ", "Giải trừ", "Cầu tế",
        "Sửa chữa lớn", "Đi xa", "Đầu tư mạo hiểm",
        "Tranh chấp kiện tụng"
    )

    // Hoàng Đạo (good) hours for each earthly branch day
    private val hoangDaoHours = arrayOf(
        intArrayOf(1, 3, 5, 7, 9, 11),    // Tý
        intArrayOf(2, 4, 6, 8, 10, 0),    // Sửu
        intArrayOf(3, 5, 7, 9, 11, 1),    // Dần
        intArrayOf(4, 6, 8, 10, 0, 2),    // Mão
        intArrayOf(5, 7, 9, 11, 1, 3),    // Thìn
        intArrayOf(6, 8, 10, 0, 2, 4),    // Tỵ
        intArrayOf(7, 9, 11, 1, 3, 5),    // Ngọ
        intArrayOf(8, 10, 0, 2, 4, 6),    // Mùi
        intArrayOf(9, 11, 1, 3, 5, 7),    // Thân
        intArrayOf(10, 0, 2, 4, 6, 8),    // Dậu
        intArrayOf(11, 1, 3, 5, 7, 9),    // Tuất
        intArrayOf(0, 2, 4, 6, 8, 10)     // Hợi
    )

    private var customGoodActivities = listOf<String>()
    private var customBadActivities = listOf<String>()
    private var customDayScores = mapOf<String, Int>()

    fun loadRules(jsonString: String?) {
        if (jsonString == null) return
        try {
            val root = JSONObject(jsonString)
            if (root.has("goodActivities")) {
                val arr = root.getJSONArray("goodActivities")
                customGoodActivities = (0 until arr.length()).map { arr.getString(it) }
            }
            if (root.has("badActivities")) {
                val arr = root.getJSONArray("badActivities")
                customBadActivities = (0 until arr.length()).map { arr.getString(it) }
            }
            if (root.has("dayScores")) {
                val arr = root.getJSONArray("dayScores")
                val map = mutableMapOf<String, Int>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val key = "${item.getInt("can")}:${item.getInt("chi")}"
                    map[key] = item.getInt("score")
                }
                customDayScores = map
            }
        } catch (_: Exception) {
        }
    }

    fun assessDay(
        lunarDay: Int,
        lunarMonth: Int,
        dayCanIndex: Int,
        dayChiIndex: Int
    ): DayAssessment {
        val key = "$dayCanIndex:$dayChiIndex"
        val baseScore = customDayScores[key] ?: 0

        val fullMoonBonus = if (lunarDay == 15) 3 else 0
        val firstDayBonus = if (lunarDay == 1) 2 else 0
        val score = baseScore + fullMoonBonus + firstDayBonus

        val isGood = score >= 0

        val label = when {
            lunarDay == 1 && lunarMonth == 1 -> "Ngày Đại Cát - Tết Nguyên Đán"
            lunarDay == 15 -> "Ngày Rằm - Tốt"
            lunarDay == 1 -> "Ngày Mùng Một - Tốt"
            isGood -> "Ngày Tốt"
            else -> "Ngày Xấu"
        }

        val activities = if (isGood) {
            (if (customGoodActivities.isNotEmpty()) customGoodActivities else defaultGoodActivities)
        } else {
            (if (customBadActivities.isNotEmpty()) customBadActivities else defaultBadActivities)
        }

        val gh = hoangDaoHours[dayChiIndex]
        val goodHoursList = gh.map { h ->
            HourInfo(
                chiName = chi[h],
                timeRange = hourTimeRanges[h],
                isGood = true,
                reason = "Hoàng Đạo"
            )
        }.sortedBy { h ->
            hourTimeRanges.indexOf(h.timeRange)
        }

        val allHours = (0..11).toSet()
        val badHoursSet = allHours - gh.toSet()
        val badHoursList = badHoursSet.sorted().map { h ->
            HourInfo(
                chiName = chi[h],
                timeRange = hourTimeRanges[h],
                isGood = false,
                reason = "Hắc Đạo"
            )
        }

        return DayAssessment(
            isGood = isGood,
            score = score,
            label = label,
            goodActivities = if (isGood) activities else defaultGoodActivities,
            badActivities = if (isGood) defaultBadActivities else activities,
            goodHours = goodHoursList,
            badHours = badHoursList
        )
    }
}
