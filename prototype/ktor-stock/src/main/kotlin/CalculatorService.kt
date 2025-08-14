package com.stock

import com.stock.stockFetcher.StockInfo
import com.stock.stockFetcher.StockPriceInfo
import com.stock.stockFetcher.USStockInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList


// KR
suspend fun custom(companies: List<StockInfo>, days: Int, amplitude: Int) = coroutineScope {
    companies
        .map {
            async(Dispatchers.IO.limitedParallelism(30)) {
                val priceInfoFlow = StockInfo
                    .getPriceFlowInfoByPage(it.code, 1, getPageByDays(amplitude))
                lightCheck(it, amplitude, priceInfoFlow)
            }
        }
        .awaitAll().filterNotNull()
        .map {
            async(Dispatchers.IO.limitedParallelism(30)) {
                val priceInfoFlow = StockInfo
                    .getPriceFlowInfoByPage(it.code, 1, getPageByDays(days))
                weightCheck(it, days, priceInfoFlow)
            }
        }.awaitAll().filterNotNull()
}

// US, 50일 이상은 계산 안됨
suspend fun usCustom(companies: List<USStockInfo>, days: Int, amplitude: Int) = coroutineScope {
    companies
        .map { StockInfo(it.name, it.code) } // 변경
        .map {
            async(Dispatchers.IO.limitedParallelism(30)) {
                val priceInfoFlow = USStockInfo.getUSPriceFlowInfo(it.code)
                lightCheck(it, amplitude, priceInfoFlow)
            }
        }
        .awaitAll().filterNotNull()
        .map {
            async(Dispatchers.IO.limitedParallelism(30)) {
                val priceInfoFlow = USStockInfo.getUSPriceFlowInfo(it.code)
                weightCheck(it, days, priceInfoFlow)
            }
        }.awaitAll().filterNotNull()
}

// 가벼운 검증은 앞에서
suspend fun lightCheck(info: StockInfo, amplitude: Int, priceInfoFlow: Flow<StockPriceInfo>): StockInfo? {
    val priceInfos = priceInfoFlow.take(amplitude).toList()

    val isAmplitude = calculateAmplitudePrice(priceInfos, 20)
    if (!isAmplitude) return null

    val volumeForThreeDay = todayIsNotMaxVolumeForThreeDay(priceInfos)
    if (!volumeForThreeDay) return null

    val consecutiveRise = consecutiveRise(priceInfos)
    if (!consecutiveRise) return null

    return info
}

// 무거운 검증은 뒤에서
suspend fun weightCheck(info: StockInfo, days: Int, priceInfoFlow: Flow<StockPriceInfo>): StockInfo? {
    val priceInfos = priceInfoFlow.take(days).toList()

    val isNewHighPrice = calculateNewHighPrice(priceInfos)
    if (!isNewHighPrice) return null

    return info
}

// 진폭
fun calculateAmplitudePrice(priceInfo: List<StockPriceInfo>, day: Int): Boolean {
    if (priceInfo.isEmpty() || priceInfo.size < day) return false
    val high = priceInfo.take(day).maxOf { it.high }
    val low = priceInfo.take(day).minOf { it.low }
    val amplitudePercent = (high - low) / low * 100
    return amplitudePercent in 10.0..30.0
}

// 거래량 3일 최대가 X
fun todayIsNotMaxVolumeForThreeDay(priceInfo: List<StockPriceInfo>): Boolean {
    if (priceInfo.isEmpty() || priceInfo.size < 3) return false
    return priceInfo[0].volume != priceInfo.take(3).maxOf { it.volume }
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

// 신고가
fun calculateNewHighPrice(priceInfo: List<StockPriceInfo>): Boolean {
    return !priceInfo.isEmpty() && priceInfo.all { it.high <= priceInfo.first().high }
}

fun getPageByDays(days: Int): Int {
    return if (days % 10 == 0) days / 10 else days / 10 + 1
}