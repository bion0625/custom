package com.stock.stockFetcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.stock.usCustom
import com.stock.util.HttpClientFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

suspend fun main() {
    val start = System.currentTimeMillis()
    val days = 20
    val amplitude = 20
    val companies = USStockInfo.getUSCompanyInfo()
    val filtered = usCustom(companies, days, amplitude)
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

data class USStockInfo(
    var name: String = "",
    var code: String = "",
    var exchange: String = ""
) {
    companion object {
        private val client = HttpClientFactory.client

        // 미국 상장 종목 목록 조회
        suspend fun getUSCompanyInfo(): List<USStockInfo> {
            val exchanges = listOf("NYSE", "NASDAQ", "AMEX")
            val mapper = jacksonObjectMapper()
            val result = mutableListOf<USStockInfo>()

            for (exchange in exchanges) {
                // 1페이지를 먼저 조회하여 전체 페이지 수 계산
                val firstUrl = "https://api.stock.naver.com/stock/exchange/$exchange/marketValue?page=1"
                val firstResp = client.get(firstUrl)
                val firstJson = mapper.readTree(firstResp.bodyAsText())
                val totalCount = firstJson["totalCount"]?.asInt() ?: 0
                val pageSize = firstJson["pageSize"]?.asInt() ?: 20
                val totalPages = (totalCount + pageSize - 1) / pageSize

                // 1페이지 종목 파싱
                firstJson["stocks"]?.forEach { node ->
                    val nameEng = node["stockNameEng"]?.asText()
                    val nameKor = node["stockName"]?.asText()
                    val code = node["symbolCode"]?.asText() ?: ""
                    val name = nameEng ?: nameKor ?: ""
                    result.add(USStockInfo(name = name, code = code, exchange = exchange))
                }
                // 나머지 페이지 반복 조회
                for (page in 2..totalPages) {
                    val url = "https://api.stock.naver.com/stock/exchange/$exchange/marketValue?page=$page"
                    val resp = client.get(url)
                    val json = mapper.readTree(resp.bodyAsText())
                    json["stocks"]?.forEach { node ->
                        val nameEng = node["stockNameEng"]?.asText()
                        val nameKor = node["stockName"]?.asText()
                        val code = node["symbolCode"]?.asText() ?: ""
                        val name = nameEng ?: nameKor ?: ""
                        result.add(USStockInfo(name = name, code = code, exchange = exchange))
                    }
                }
            }
            return result
        }

        suspend fun getUSPriceFlowInfo(ticker: String): Flow<StockPriceInfo> {
            return flow {
                getUSPriceInfo(ticker).forEach {
                    emit(it)
                }
            }
        }

        // 미국 종목의 일별 시세 조회 (최근 6개월)
        suspend fun getUSPriceInfo(ticker: String): List<StockPriceInfo> {
            val url = "https://stockanalysis.com/stocks/${ticker.lowercase()}/history/"
            val response: HttpResponse = client.get(url)
            val html = response.bodyAsText()
            val doc = Jsoup.parse(html)
            val rows = doc.select("table tbody tr")
            val results = mutableListOf<StockPriceInfo>()

            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
            for (row in rows) {
                val cols = row.select("td")
                if (cols.size >= 8) {
                    try {
                        val dateText = cols[0].text()
                        val openText = cols[1].text()
                        val highText = cols[2].text()
                        val lowText = cols[3].text()
                        val closeText = cols[4].text()
                        val volumeText = cols[7].text()

                        val localDate = LocalDate.parse(dateText, formatter)
                        val date: Date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        val open = FormatUtil.stringToDouble(openText)
                        val high = FormatUtil.stringToDouble(highText)
                        val low  = FormatUtil.stringToDouble(lowText)
                        val close = FormatUtil.stringToDouble(closeText)
                        val volume = FormatUtil.stringToDouble(volumeText)
                        // 등락폭은 간단히 종가-시가로 계산
                        val diff = close - open

                        results.add(StockPriceInfo(date.toString(), close, open, high, low, volume, diff))
                    } catch (e: Exception) {
                        // 형식이 맞지 않는 경우 건너뜀
                    }
                }
            }
            return results
        }
    }
}
