package com.stxtory.lietzsche

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@Configuration
class SecurityConfig {
    @Bean
    fun chain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/health", "/actuator/info").permitAll()
                it.pathMatchers("/actuator/**").hasRole("ADMIN")
                it.pathMatchers("/api/**").permitAll()
                it.anyExchange().authenticated()
            }
            .httpBasic { }              // ✅ Actuator용 Basic 활성화
            .build()
}