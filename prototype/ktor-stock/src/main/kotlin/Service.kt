package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun getNewHighPriceStock() = coroutineScope {
    val start = System.currentTimeMillis()
    val page = 25
    val companies = StockInfo.getCompanyInfo()
    val filtered = companies.map {
        async {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (priceInfo.isEmpty()) return@async null
            val todayHigh = priceInfo.first().high
            if (priceInfo.all { it.high <= todayHigh }) it else null
        }
    }.awaitAll()
        .filterNotNull()
    val end = System.currentTimeMillis()

    """
        size: ${companies.size}
                ${page * 10}일 기준 신고가 종목
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()
}

suspend fun getAmplitudePriceStock() = coroutineScope {
    val start = System.currentTimeMillis()
    val page = 2 // 20일 기준
    val companies = StockInfo.getCompanyInfo()
    val filtered = companies.map {
        async {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (priceInfo.isEmpty() || priceInfo.size < 2) return@async null
            val high = priceInfo.maxOf { it.high }
            val low = priceInfo.minOf { it.low }

            val amplitudePercent = (high - low) / low * 100
            if (amplitudePercent in 10.0..30.0) it else null
        }
    }.awaitAll()
        .filterNotNull()
    val end = System.currentTimeMillis()

    """
        size: ${companies.size}
                ${page * 10}일 기준 진폭 종목
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()
}