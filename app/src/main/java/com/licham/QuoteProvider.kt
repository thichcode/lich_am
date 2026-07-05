package com.licham

import org.json.JSONArray

data class Quote(
    val text: String,
    val author: String
)

object QuoteProvider {
    private var quotes = listOf(
        Quote("Có công mài sắt, có ngày nên kim.", "Tục ngữ Việt Nam"),
        Quote("Ăn quả nhớ kẻ trồng cây.", "Tục ngữ Việt Nam"),
        Quote("Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao.", "Tục ngữ Việt Nam")
    )

    fun loadQuotes(jsonString: String?) {
        if (jsonString == null) return
        try {
            val arr = JSONArray(jsonString)
            quotes = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Quote(
                    text = obj.getString("text"),
                    author = obj.optString("author", "Khuyết danh")
                )
            }
        } catch (_: Exception) {
        }
    }

    fun getRandomQuote(): Quote {
        return quotes.random()
    }
}
