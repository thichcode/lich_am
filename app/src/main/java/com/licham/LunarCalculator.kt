package com.licham

import kotlin.math.*

data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeap: Boolean = false
)

object LunarCalculator {

    fun jdFromDate(dd: Int, mm: Int, yy: Int): Long {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        return dd.toLong() + (153L * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045L
    }

    fun jdToDate(jd: Long): Triple<Int, Int, Int> {
        val a = jd + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = (e - (153 * m + 2) / 5 + 1).toInt()
        val month = (m + 3 - 12 * (m / 10)).toInt()
        val year = (b * 100 + d - 4800 + (m / 10)).toInt()
        return Triple(day, month, year)
    }

    private fun sunLongitude(jd: Long): Double {
        val t = (jd - 2451545.0) / 36525.0
        val t2 = t * t
        val m = 357.5291 + 35999.0503 * t - 0.0001559 * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.9146 - 0.004817 * t - 0.000014 * t2) * sin(m * PI / 180)
        dl += (0.019993 - 0.000101 * t) * sin(2 * m * PI / 180)
        dl += 0.00029 * sin(3 * m * PI / 180)
        var l = l0 + dl
        l = l - 360 * floor(l / 360)
        if (l < 0) l += 360
        return l
    }

    private fun newMoonDay(k: Int): Long {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        var jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * PI / 180)
        val m = 2.5534 + 29.10535669 * k - 0.0000218 * t2 - 0.00000011 * t3
        val mp = 160.7108 + 390.67050274 * k - 0.0016341 * t2 - 0.00000227 * t3
        val f = 201.5643 + 385.81693528 * k + 0.0107438 * t2 + 0.00001239 * t3
        jd += 1.122 * sin(m * PI / 180)
        jd -= 0.406 * sin((mp - m) * PI / 180)
        jd += 0.541 * sin(2 * f * PI / 180)
        jd -= 0.166 * sin(2 * m * PI / 180)
        jd += 0.207 * sin(2 * mp * PI / 180)
        jd += 0.136 * sin(2 * (mp - m) * PI / 180)
        jd -= 0.049 * sin(2 * (mp + m) * PI / 180)
        return round(jd).toLong()
    }

    private fun getLunarMonth11(yy: Int): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        var k = floor(off / 29.530588853).toInt()
        while (sunLongitude(newMoonDay(k)) <= 270) k++
        while (sunLongitude(newMoonDay(k - 1)) > 270) k--
        return k
    }

    private fun getLeapMonthOffset(a11: Int): Int {
        var i = 1
        while (i < 15) {
            val sl1 = sunLongitude(newMoonDay(a11 + i))
            val sl2 = sunLongitude(newMoonDay(a11 + i + 1))
            if (floor(sl1 / 30).toInt() == floor(sl2 / 30).toInt()) {
                return i
            }
            i++
        }
        return -1
    }

    fun solar2lunar(dd: Int, mm: Int, yy: Int): LunarDate? {
        val jd = jdFromDate(dd, mm, yy)
        val k0 = floor((jd - 2415021) / 29.530588853).toInt()
        var k = k0
        var nm = newMoonDay(k)
        if (nm > jd) {
            k = k0 - 1
            nm = newMoonDay(k)
        }
        val lunarDay = (jd - nm + 1).toInt()

        val k11 = getLunarMonth11(yy)
        val k11Prev = getLunarMonth11(yy - 1)

        var lunarYear: Int
        var monthOffset: Int

        when {
            k >= k11 -> {
                lunarYear = yy
                monthOffset = k - k11
            }
            k >= k11Prev -> {
                lunarYear = yy - 1
                monthOffset = k - k11Prev
            }
            else -> {
                val k11Prev2 = getLunarMonth11(yy - 2)
                lunarYear = yy - 2
                monthOffset = k - k11Prev2
            }
        }

        if (monthOffset < 0) return null

        val leapOffset = getLeapMonthOffset(
            if (k >= k11) k11 else k11Prev
        )

        var lunarMonth = monthOffset + 11
        var isLeap = false

        if (leapOffset > 0 && monthOffset >= leapOffset) {
            if (monthOffset == leapOffset) {
                isLeap = true
                lunarMonth = leapOffset + 10
                if (lunarMonth > 12) {
                    lunarMonth -= 12
                }
            } else {
                lunarMonth = monthOffset + 10
            }
        }

        if (lunarMonth > 12) {
            lunarMonth -= 12
        }

        return LunarDate(lunarDay, lunarMonth, lunarYear, isLeap)
    }

    fun lunar2solar(lunarDay: Int, lunarMonth: Int, lunarYear: Int, isLeap: Boolean): Triple<Int, Int, Int>? {
        val baseYear = if (lunarMonth >= 11) lunarYear else lunarYear - 1
        val k11 = getLunarMonth11(baseYear)
        val targetOffset = if (lunarMonth >= 11) lunarMonth - 11 else lunarMonth + 1

        for (tryOffset in (targetOffset - 1)..(targetOffset + 1)) {
            if (tryOffset < 0) continue
            val k = k11 + tryOffset
            val nm = newMoonDay(k)
            val jd = nm + lunarDay - 1
            if (jd < 0) continue
            val (d, m, y) = jdToDate(jd)
            val result = solar2lunar(d, m, y)
            if (result != null &&
                result.day == lunarDay &&
                result.month == lunarMonth &&
                result.year == lunarYear &&
                result.isLeap == isLeap
            ) {
                return Triple(d, m, y)
            }
        }
        return null
    }

    fun getLunarMonthName(month: Int, isLeap: Boolean): String {
        val names = arrayOf(
            "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu",
            "Bảy", "Tám", "Chín", "Mười", "Một", "Chạp"
        )
        val prefix = if (isLeap) "Tháng nhuận " else "Tháng "
        return prefix + names[month - 1]
    }
}
