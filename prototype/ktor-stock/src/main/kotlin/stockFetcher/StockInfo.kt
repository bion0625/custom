package com.stock.stockFetcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.stock.custom
import com.stock.getPageByDays
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
    val start = System.currentTimeMillis()
    val page = getPageByDays(20)
    val companies = StockInfo.getUSCompanyInfo()
    val filtered = custom(companies, page, 20)
    val end = System.currentTimeMillis()

    // todo 미국 종목 현재 네이버에서 가격 정보 못가져오고 있음

    val result = """
        size: ${companies.size}
                ${page * 10}일 기준 신고가
                ${20}일 기준 진폭 10~30%
                오늘이 거래량 최대가 X
                최근 3일 연달아 상승
                
                ${"\n" + filtered.joinToString("\n") { "- - - - - - -> ${it.name}" }}
                
                전체 걸린 시간: ${(end - start) / 1000.0}s
    """.trimIndent()

    println(result)
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

        suspend fun getUSCompanyInfo(): List<StockInfo> {
            val exchanges = listOf("NYSE", "NASDAQ", "AMEX")
            return exchanges.flatMap { exchange ->
                getUSCompanyInfoByExchange(exchange)
            }
        }

        private suspend fun getUSCompanyInfoByExchange(exchange: String): List<StockInfo> {
            val mapper = jacksonObjectMapper()
            val result = mutableListOf<StockInfo>()

            // 먼저 1페이지를 조회하여 전체 건수와 페이지 크기를 확인
            val firstUrl = "https://api.stock.naver.com/stock/exchange/$exchange/marketValue?page=1"
            val firstResp = client.get(firstUrl)
            val firstJson = mapper.readTree(firstResp.bodyAsText())
            val totalCount = firstJson["totalCount"]?.asInt() ?: 0
            val pageSize = firstJson["pageSize"]?.asInt() ?: 20
            val totalPages = (totalCount + pageSize - 1) / pageSize

            // page=1의 종목 목록 파싱
            firstJson["stocks"]?.forEach { node ->
                val name = node["stockName"]?.asText() ?: ""
                val code = node["symbolCode"]?.asText() ?: ""
                result.add(StockInfo(name = name, code = code))
            }

            // 나머지 페이지 반복 호출
            for (page in 2..totalPages) {
                val url = "https://api.stock.naver.com/stock/exchange/$exchange/marketValue?page=$page"
                val resp = client.get(url)
                val json = mapper.readTree(resp.bodyAsText())
                json["stocks"]?.forEach { node ->
                    val name = node["stockName"]?.asText() ?: ""
                    val code = node["symbolCode"]?.asText() ?: ""
                    result.add(StockInfo(name = name, code = code))
                }
            }
            return result
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
