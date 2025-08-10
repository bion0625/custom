package com.stock.stockFetcher

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

suspend fun main() {
    println(StockInfo.getCompanyInfo().size)
    println(StockInfo.getCompanyInfo().size)
    println(StockInfo.getCompanyInfo().size)
}

data class StockInfo(
    var name: String = "",
    var code: String = ""
) {

    companion object {
        private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 12_000
                socketTimeoutMillis  = 20_000
                requestTimeoutMillis = 25_000
            }
            install(HttpRequestRetry) {
                retryOnExceptionIf(maxRetries = 3) { request, cause ->
                    // 읽기/요청 타임아웃, 네트워크 IO 오류는 재시도
                    cause is HttpRequestTimeoutException || cause is java.io.IOException
                }
                retryIf(maxRetries = 3) { request, response ->
                    val s = response.status.value
                    // 5xx, 429 재시도
                    s == 429 || s in 500..599
                }
                delayMillis { retry -> 300L shl (retry - 1) } // 300, 600, 1200
                modifyRequest { // 429 등에서 헤더 보존
                    it.headers.appendIfNameAbsent("User-Agent", "Mozilla/5.0")
                }
            }
            defaultRequest {
                headers.append("User-Agent", "Mozilla/5.0")
                // 선택: 네이버는 referer 있으면 더 관대할 때가 있음
                // url.queryParameters["code"]?.let { code ->
                //     headers.append("Referer", "https://finance.naver.com/item/sise.nhn?code=$code")
                // }
            }
            engine{
                maxConnectionsCount = 500
                endpoint {
                    maxConnectionsPerRoute = 300
                    keepAliveTime = 5_000
                    connectAttempts = 3
                    connectTimeout = 15_000
                    pipelineMaxSize = 20
                }
            }
        }

        suspend fun getCompanyInfo(): List<StockInfo> {
            return listOf("stockMkt", "kosdaqMkt")
                .map {getCompanyInfo(it)}
                .flatten()
        }

        suspend fun getCompanyInfo(marketType: String): List<StockInfo> {
            val url = "http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13&marketType=$marketType"
            val response: HttpResponse = client.get(url)
            val html = response.bodyAsText()
            val doc = Jsoup.parse(html)

            return doc.select("tr")
                .drop(1)
                .mapNotNull { row ->
                    val cols = row.select("td")
                    if (cols.size >= 2) {
                        StockInfo(name = cols[0].text(), code = cols[1].text())
                    } else null
                }
        }

        suspend fun getPriceFlowInfoByPage(code: String, from: Int, to: Int): Flow<StockPriceInfo> {
            return flow {
                (from .. to).map { page ->
                    getPriceInfo(code, page).forEach {
                        emit(it)
                    }
                }
            }
        }

        suspend fun getPriceInfoByPage(code: String, from: Int, to: Int): List<StockPriceInfo> {
            return (from..to).map { page -> getPriceInfo(code, page) }.flatten()
        }

        suspend fun getPriceInfo(code: String, page: Int): List<StockPriceInfo> {
            val url = "https://finance.naver.com/item/sise_day.nhn?code=$code&page=$page"
            return try {
                val response = client.get(url)
                val html = response.bodyAsText()
                val doc = Jsoup.parse(html)
                val rows = doc.select("tr")

                rows.drop(2).dropLast(2)
                    .filter { it.select("td").size >= 7 }
                    .mapNotNull { row ->
                        val cols = row.select("td")
                        try {
                            val date = FormatUtil.stringToDate(cols[0].text())
                            val close = FormatUtil.stringToDouble(cols[1].text())
                            val open = FormatUtil.stringToDouble(cols[3].text())
                            val high = FormatUtil.stringToDouble(cols[4].text())
                            val low = FormatUtil.stringToDouble(cols[5].text())
                            val volume = FormatUtil.stringToDouble(cols[6].text())
                            val diffRaw = cols[2].text()
                            val isMinus = diffRaw.contains("하락") || diffRaw.contains("하한가")
                            var diff = FormatUtil.stringToDouble(
                                diffRaw.replace("상승", "").replace("하락", "")
                                    .replace("상한가", "").replace("하한가", "").replace("보합", "").trim()
                            )
                            if (isMinus) diff *= -1

                            StockPriceInfo(date, close, open, high, low, volume, diff)
                        } catch (e: Exception) {
                            null
                        }
                    }

            } catch (e: Exception) {
                println(e)
                emptyList()
            }
        }
    }
}
