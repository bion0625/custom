package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val companies = StockInfo.getCompanyInfo()
    val filtered = companies.filter {
        val priceInfo = StockInfo.getPriceInfoByPage(it.code, 1, 13)
        if (priceInfo.isEmpty()) return@filter false;
        val todayHigh = priceInfo.first().high
        priceInfo.all { it.high < todayHigh }
    }
    filtered.forEach { println("금일 신고가 종목: ${it.name}") }
}