package com.stxtory.admin

import de.codecentric.boot.admin.server.config.EnableAdminServer
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

@EnableAdminServer
@SpringBootApplication
class AdminApplication

@Bean
fun actuatorAuthHeaders(): HttpHeadersProvider = HttpHeadersProvider { _ ->
	HttpHeaders().apply {
		setBasicAuth("actuator", "actuator-pass")
	}
}

fun main(args: Array<String>) {
	runApplication<AdminApplication>(*args)
}
