package com.stock.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*

object HttpClientFactory {
    val client = HttpClient(CIO) {
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
}