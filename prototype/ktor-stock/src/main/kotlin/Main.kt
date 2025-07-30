package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val page = 13

    val companies = StockInfo.getCompanyInfo()
    println("size: ${companies.size}")
    val filtered = companies.map { company ->
        async {
            val priceInfo = StockInfo.getPriceInfoByPage(company.code, 1, page)
            if (priceInfo.isEmpty()) return@async null
            val todayHigh = priceInfo.first().high
            if (priceInfo.all { it.high <= todayHigh }) company else null
        }
    }.awaitAll()
        .filterNotNull()
    filtered.forEach { println("${page*10}일 기준 신고가 종목: ${it.name}") }
}