package com.stock

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/custom/{amplitude}/{days}") {
            val days = call.parameters["days"]?.toIntOrNull()
                ?: return@get call.respondText("Invalid days", status = HttpStatusCode.BadRequest)
            val amplitude = call.parameters["amplitude"]?.toIntOrNull()
                ?: return@get call.respondText("Invalid amplitude", status = HttpStatusCode.BadRequest)
            call.respondText(custom(days, amplitude))
        }
    }
}
