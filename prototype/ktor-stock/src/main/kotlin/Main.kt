package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val start = System.currentTimeMillis()

    val page = 25

    val companies = StockInfo.getCompanyInfo()
    println("size: ${companies.size}")
    val filtered = companies.map {
        async {
            val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, page)
            if (priceInfo.isEmpty()) return@async null
            val todayHigh = priceInfo.first().high
            if (priceInfo.all { it.high <= todayHigh }) it else null
        }
    }.awaitAll()
        .filterNotNull()
    filtered.forEach { println("${page*10}일 기준 신고가 종목: ${it.name}") }

    val end = System.currentTimeMillis()

    println("전체 걸린 시간: ${end-start}ms")
}