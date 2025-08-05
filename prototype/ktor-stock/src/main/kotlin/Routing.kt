package com.stock

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/new-high-price") { call.respondText(getNewHighPriceStock()) }
    }
    routing {
        get("/amplitude") { call.respondText(getAmplitudePriceStock()) }
    }
}
