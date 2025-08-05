package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun calculateNewHighPriceStock(companies: List<StockInfo>, page: Int) = coroutineScope {
    companies.map {
        async {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (priceInfo.isEmpty()) return@async null
            val todayHigh = priceInfo.first().high
            if (priceInfo.all { it.high <= todayHigh }) it else null
        }
    }.awaitAll()
        .filterNotNull()
}

suspend fun calculateAmplitudePriceStock(companies: List<StockInfo>, page: Int) = coroutineScope {
    companies.map {
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
}