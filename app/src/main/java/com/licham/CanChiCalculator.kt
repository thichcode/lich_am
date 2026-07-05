package com.licham

data class CanChi(
    val day: Pair<String, String>,
    val month: Pair<String, String>,
    val year: Pair<String, String>,
    val hour: Pair<String, String>
)

object CanChiCalculator {
    private val can = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val chi = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    private val hourChiIndex = arrayOf(
        0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5,
        6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11
    )

    fun getCanChiHour(hour: Int, dayCanIndex: Int): Pair<String, String> {
        val hourChi = hourChiIndex[hour % 24]
        val hourCan = (dayCanIndex * 2 + hourChi) % 10
        return Pair(can[hourCan], chi[hourChi])
    }

    fun getHourChiName(hour: Int): String {
        return chi[hourChiIndex[hour % 24]]
    }

    fun getDayCanChi(jd: Long): Pair<String, String> {
        val canIndex = ((jd + 9) % 10).toInt()
        val chiIndex = ((jd - 1) % 12).toInt()
        return Pair(can[canIndex], chi[chiIndex])
    }

    fun getMonthCanChi(lunarMonth: Int, yearCanIndex: Int): Pair<String, String> {
        val monthCan = (lunarMonth + yearCanIndex * 2 + 1) % 10
        val monthChi = (lunarMonth + 1) % 12
        return Pair(can[monthCan], chi[monthChi])
    }

    fun getYearCanChi(year: Int): Pair<String, String> {
        val canIndex = (year - 4) % 10
        val chiIndex = (year - 4) % 12
        return Pair(can[canIndex], chi[chiIndex])
    }

    fun getYearCanIndex(year: Int): Int {
        return ((year - 4) % 10 + 10) % 10
    }

    fun formatCanChi(canChi: Pair<String, String>): String {
        return "${canChi.first} ${canChi.second}"
    }
}
