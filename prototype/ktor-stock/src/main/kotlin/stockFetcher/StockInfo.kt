package com.stock.stockFetcher

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.time.LocalDateTime

data class StockInfo(
    var name: String = "",
    var code: String = "",
    var totalPage: Int = 0,
    var prices: List<StockPriceInfo> = emptyList(),
    var originMinimumSellingPrice: Double = 0.0,
    var originExpectedSellingPrice: Double = 0.0,
    var minimumSellingPrice: Double = 0.0,
    var expectedSellingPrice: Double = 0.0,
    var tempPrice: Double = 0.0,
    var settingPrice: Double = 0.0,
    var updatedAt: LocalDateTime? = null,
    var pricingReferenceDate: LocalDateTime? = null,
    var renewalCnt: Int = 0
) {
    fun toEntity(highPer: Double, lowPer: Double): Stock {
        val temp = prices.firstOrNull()?.close ?: 0.0
        val setting = temp
        val minimum = setting * lowPer
        val expected = setting * highPer
        return Stock(code, name, minimum, expected, minimum, expected, temp, setting)
    }

    fun sellingPriceUpdate(highPer: Double, lowPer: Double) {
        this.minimumSellingPrice = this.expectedSellingPrice * lowPer
        this.expectedSellingPrice = this.expectedSellingPrice * highPer
        this.renewalCnt++
        this.pricingReferenceDate = LocalDateTime.now()
    }

    companion object {
        private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
        }

        suspend fun getCompanyInfo(): List<StockInfo> {
            val url = "http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13"
            val response: HttpResponse = client.get(url) {
                headers.append("User-Agent", "Mozilla/5.0")
            }
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

//            return coroutineScope {
//                stocks.map { stock ->
//                    async {
//                        if (isIdentifier(stock.code)) stock else null
//                    }
//                }.awaitAll().filterNotNull()
//            }
        }

//        private suspend fun isIdentifier(code: String): Boolean {
//            val url = "https://finance.naver.com/item/main.naver?code=$code"
//            return try {
//                val response: HttpResponse = client.get(url)
//                val html = response.bodyAsText()
//                val doc = Jsoup.parse(html)
//
//                val kospi = doc.select("img.kospi[alt=코스피]").isNotEmpty()
//                val kosdaq = doc.select("img.kosdaq[alt=코스닥]").isNotEmpty()
//                kospi || kosdaq
//            } catch (e: Exception) {
//                false
//            }
//        }

        suspend fun getPriceInfoByPage(code: String, from: Int, to: Int): List<StockPriceInfo> {
            return (from..to).map { page -> getPriceInfo(code, page) }.flatten()
        }

        suspend fun getPriceInfo(code: String, page: Int): List<StockPriceInfo> {
            val url = "https://finance.naver.com/item/sise_day.nhn?code=$code&page=$page"
            return try {
                val response = client.get(url) {
                    headers.append("User-Agent", "Mozilla/5.0")
                }
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
                emptyList()
            }
        }
    }
}
