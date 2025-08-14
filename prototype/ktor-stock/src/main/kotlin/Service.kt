package com.stock

import com.stock.stockFetcher.StockInfo
import kotlinx.coroutines.coroutineScope

// 20일 기준 진폭
suspend fun custom(days: Int, amplitude: Int) = coroutineScope {
    val start = System.currentTimeMillis()
    val companies = StockInfo.getCompanyInfo()
    val filtered = custom(companies, days, amplitude)
    val end = System.currentTimeMillis()

    """
        size: ${companies.size}
                ${days}일 기준 신고가
                ${amplitude}일 기준 진폭 10~30%
                오늘이 거래량 최대가 X
                최근 3일 연달아 상승
                
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()
}