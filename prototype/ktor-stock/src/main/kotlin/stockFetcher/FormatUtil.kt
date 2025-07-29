package com.stock.stockFetcher

object FormatUtil {
    fun stringToDouble(text: String): Double {
        return text.replace(",", "").trim().toDoubleOrNull() ?: 0.0
    }

    fun stringToDate(text: String): String {
        return text.trim() // 가공 필요 시 변경
    }
}
