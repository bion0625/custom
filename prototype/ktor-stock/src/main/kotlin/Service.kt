package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.coroutineScope

suspend fun getNewHighPriceStock() = coroutineScope {
    val start = System.currentTimeMillis()
    val page = 25
    val companies = StockInfo.getCompanyInfo()
    val filtered = calculateNewHighPriceStock(companies, page)
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
    val filtered = calculateAmplitudePriceStock(companies, page)
    val end = System.currentTimeMillis()

    """
        size: ${companies.size}
                ${page * 10}일 기준 진폭 종목
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()
}

suspend fun getNewHighAndAmplitudePriceStock() = coroutineScope {
    val start = System.currentTimeMillis()
    val page = 25 // 250일 기준 신고가, 20일 기준 진폭
    val companies = StockInfo.getCompanyInfo()
    val filtered = all(companies, page)
    val end = System.currentTimeMillis()

    """
        size: ${companies.size}
                ${page * 10}일 기준 신고가
                20일 기준 진폭 10~30%
                오늘이 거래량 최대가 X
                최근 3일 연달아 상승
                
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()
}