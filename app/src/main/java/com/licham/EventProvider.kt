package com.licham

import org.json.JSONArray
import java.io.InputStream
import java.time.LocalDate

object EventProvider {
    private var events = listOf<Event>()

    fun load(jsonStream: InputStream?) {
        if (jsonStream == null) return
        try {
            val text = jsonStream.bufferedReader().readText()
            val arr = JSONArray(text)
            events = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Event(
                    type = obj.getString("type"),
                    day = obj.getInt("day"),
                    month = obj.getInt("month"),
                    name = obj.getString("name")
                )
            }
        } catch (_: Exception) {}
    }

    fun getTodayEvents(solarDay: Int, solarMonth: Int, lunarDay: Int, lunarMonth: Int): List<String> {
        val result = mutableListOf<String>()
        for (e in events) {
            if (e.type == "solar" && e.day == solarDay && e.month == solarMonth) {
                result.add(e.name)
            }
            if (e.type == "lunar" && e.day == lunarDay && e.month == lunarMonth) {
                result.add(e.name)
            }
        }
        return result
    }

    private data class Event(
        val type: String,
        val day: Int,
        val month: Int,
        val name: String
    )
}
