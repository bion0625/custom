package com.stock.stockFetcher

import com.stock.util.HttpClientFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

data class StockInfo(
    var name: String = "",
    var code: String = ""
) {

    companion object {
        private val client = HttpClientFactory.client

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
                (from .. to).forEach { page ->
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
