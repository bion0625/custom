package com.stock

import com.stock.stockFetcher.StockInfo
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

fun Application.configureRouting() {
    routing {
        get("/") {
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

            val result = """
                size: ${companies.size}
                ${page * 10}일 기준 신고가 종목
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
            """.trimIndent()
            call.respondText(result)
        }
    }
}
