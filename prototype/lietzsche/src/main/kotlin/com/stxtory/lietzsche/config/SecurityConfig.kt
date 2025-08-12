package com.stxtory.lietzsche.config

import com.stxtory.lietzsche.service.JwtService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter

@EnableConfigurationProperties(JwtProperties::class)
@EnableWebFluxSecurity
@Configuration
class SecurityConfig(private val jwtService: JwtService) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

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

    @Bean
    fun jwtWebFilter(): WebFilter = WebFilter { exchange: ServerWebExchange, chain ->
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader?.startsWith("Bearer ") == true) {
            val token = authHeader.removePrefix("Bearer ").trim()
            try {
                val username = jwtService.parseUsername(token)
                val roles = jwtService.parseRoles(token)
                val authorities = roles.map { SimpleGrantedAuthority(it) }
                val auth = UsernamePasswordAuthenticationToken(username, null, authorities)

                return@WebFilter chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            } catch (_: Exception) {
                // 토큰 문제면 비인증 상태로 통과 -> 이후 401
            }
        }
        chain.filter(exchange)
    }
}