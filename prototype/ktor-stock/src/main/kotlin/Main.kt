package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val companies = StockInfo.getCompanyInfo()
    println("종목 수: ${companies.size}")
    println(companies.take(10))
}