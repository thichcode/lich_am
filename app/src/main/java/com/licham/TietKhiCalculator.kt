package com.licham

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.roundToLong

data class TermInfo(
    val name: String,
    val date: LocalDate
)

object TietKhiCalculator {
    val termNames = arrayOf(
        "Lập Xuân", "Vũ Thủy", "Kinh Trập", "Xuân Phân",
        "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn",
        "Mang Chủng", "Hạ Chí", "Tiểu Thử", "Đại Thử",
        "Lập Thu", "Xử Thử", "Bạch Lộ", "Thu Phân",
        "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết",
        "Đại Tuyết", "Đông Chí", "Tiểu Hàn", "Đại Hàn"
    )

    private val termLongitudes = doubleArrayOf(
        315.0, 330.0, 345.0, 0.0, 15.0, 30.0,
        45.0, 60.0, 75.0, 90.0, 105.0, 120.0,
        135.0, 150.0, 165.0, 180.0, 195.0, 210.0,
        225.0, 240.0, 255.0, 270.0, 285.0, 300.0
    )

    private val termDayOffsets = doubleArrayOf(
        33.0, 48.0, 63.0, 79.0, 94.0, 110.0,
        125.0, 141.0, 156.0, 172.0, 187.0, 203.0,
        219.0, 235.0, 250.0, 265.0, 281.0, 296.0,
        311.0, 326.0, 340.0, 355.0, 4.0, 19.0
    )

    fun degToRad(deg: Double): Double = deg * PI / 180.0

    fun getSunLongitude(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        val L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T * T
        val M = degToRad(357.52911 + 35999.05029 * T - 0.0001537 * T * T)
        val correction = (
            (1.914602 - 0.004817 * T - 0.000014 * T * T) * sin(M) +
                (0.019993 - 0.000101 * T) * sin(2.0 * M) +
                0.000289 * sin(3.0 * M)
            )
        val lambda = L0 + correction
        return ((lambda % 360.0) + 360.0) % 360.0
    }

    private fun findTermJD(cycleYear: Int, termIndex: Int): Double {
        val targetLon = termLongitudes[termIndex]
        val termYear = if (termIndex >= 22) cycleYear + 1 else cycleYear
        val jan1JD = LunarCalculator.jdFromDate(1, 1, termYear).toDouble()
        val approxJD = jan1JD + termDayOffsets[termIndex]
        return refineJD(targetLon, approxJD)
    }

    private fun refineJD(targetLon: Double, approxJD: Double): Double {
        var jd = approxJD
        for (i in 0..40) {
            val lon = getSunLongitude(jd)
            var diff = (targetLon - lon + 540.0) % 360.0 - 180.0
            if (kotlin.math.abs(diff) < 0.001) break
            jd += diff * 1.0
        }
        return jd
    }

    private fun findCurrentTermIndex(sunLon: Double): Int {
        val norm = ((sunLon - 315.0) % 360.0 + 360.0) % 360.0
        return (norm / 15.0).toInt() % 24
    }

    private fun jdToLocalDate(jd: Double): LocalDate {
        val (d, m, y) = LunarCalculator.jdToDate(jd.roundToLong())
        return LocalDate.of(y, m, d)
    }

    fun getCurrentAndNext(date: LocalDate): Pair<TermInfo, TermInfo>? {
        val jd = LunarCalculator.jdFromDate(
            date.dayOfMonth, date.monthValue, date.year
        ).toDouble()
        val lon = getSunLongitude(jd)
        val idx = findCurrentTermIndex(lon)
        val nextIdx = (idx + 1) % 24
        val cycleYear = if (idx >= 22) date.year - 1 else date.year

        val currentJD = findTermJD(cycleYear, idx)
        val currentDate = jdToLocalDate(currentJD)
        val currentTerm = TermInfo(termNames[idx], currentDate)

        val nextCycleYear = when {
            nextIdx == 0 -> cycleYear + 1
            nextIdx >= 22 && idx < 22 -> cycleYear
            nextIdx <= idx -> cycleYear + 1
            else -> cycleYear
        }
        val nextJD = findTermJD(nextCycleYear, nextIdx)
        val nextDate = jdToLocalDate(nextJD)
        val nextTerm = TermInfo(termNames[nextIdx], nextDate)

        return currentTerm to nextTerm
    }
}
