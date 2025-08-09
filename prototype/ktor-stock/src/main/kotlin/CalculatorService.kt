package com.stock

import com.stock.stockFetcher.StockInfo
import com.stock.stockFetcher.StockPriceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun calculateNewHighPriceStock(companies: List<StockInfo>, page: Int) = coroutineScope {
    companies.map {
        async(Dispatchers.IO.limitedParallelism(30)) {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (calculateNewHighPrice(priceInfo)) it else null
        }
    }.awaitAll()
        .filterNotNull()
}

suspend fun calculateAmplitudePriceStock(companies: List<StockInfo>, page: Int) = coroutineScope {
    companies.map {
        async(Dispatchers.IO.limitedParallelism(30)) {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (calculateAmplitudePrice(priceInfo, 2)) it else null
        }
    }.awaitAll()
        .filterNotNull()
}

suspend fun all(companies: List<StockInfo>, page: Int) = coroutineScope {
    companies.map {
        async(Dispatchers.IO.limitedParallelism(30)) {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)

            val isAmplitude = calculateAmplitudePrice(priceInfo, 2)
            if (!isAmplitude) return@async null

            val volumeForThreeDay = todayIsNotMaxVolumeForThreeDay(priceInfo)
            if (!volumeForThreeDay) return@async null

            val consecutiveRise = consecutiveRise(priceInfo)
            if (!consecutiveRise) return@async null

            val isNewHighPrice = calculateNewHighPrice(priceInfo)
            if (!isNewHighPrice) return@async null

            it
        }
    }.awaitAll()
        .filterNotNull()
}

suspend fun main() {
    val echoAndDream = "101360"
    val priceInfo = StockInfo.getPriceInfoByPage(echoAndDream, 1, 25)

    val isNewHighPrice = calculateNewHighPrice(priceInfo)
    if (!isNewHighPrice) println("${echoAndDream}: !isNewHighPrice")

    val isAmplitude = calculateAmplitudePrice(priceInfo, 2)
    if (!isAmplitude) println("${echoAndDream}: !isAmplitude")

    val volumeForThreeDay = todayIsNotMaxVolumeForThreeDay(priceInfo)
    if (!volumeForThreeDay) println("${echoAndDream}: !volumeForThreeDay")

    val consecutiveRise = consecutiveRise(priceInfo)
    if (!consecutiveRise) println("${echoAndDream}: !consecutiveRise")

    println("${echoAndDream}: Success!")
}

// 신고가
fun calculateNewHighPrice(priceInfo: List<StockPriceInfo>): Boolean {
    return !priceInfo.isEmpty() && priceInfo.all { it.high <= priceInfo.first().high }
}

// 진폭
fun calculateAmplitudePrice(priceInfo: List<StockPriceInfo>, day: Int): Boolean {
    if (priceInfo.isEmpty() || priceInfo.size < day) return false
    val high = priceInfo.subList(0, day).maxOf { it.high }
    val low = priceInfo.subList(0, day).minOf { it.low }
    val amplitudePercent = (high - low) / low * 100
    return amplitudePercent in 10.0..30.0
}

// 거래량 3일 최대가 X
fun todayIsNotMaxVolumeForThreeDay(priceInfo: List<StockPriceInfo>): Boolean {
    if (priceInfo.isEmpty() || priceInfo.size < 3) return false
    return priceInfo[0].volume != priceInfo.maxOf { it.volume }
}

// 고가, 저가 3일 연달아 상승
fun consecutiveRise(priceInfo: List<StockPriceInfo>): Boolean {
    if (priceInfo.isEmpty() || priceInfo.size < 3) return false
    if (priceInfo[0].high > priceInfo[1].high && priceInfo[1].high > priceInfo[2].high
        && priceInfo[0].low > priceInfo[1].low && priceInfo[1].low > priceInfo[2].low) {
        return true
    }
    return false
}
