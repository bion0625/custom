package com.stxtory.lietzsche.config

import com.stxtory.lietzsche.service.JwtService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter

@EnableConfigurationProperties(JwtProperties::class)
@EnableWebFluxSecurity
@Configuration
class SecurityConfig(private val jwtService: JwtService) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    // 액추에이터용 ADMIN 유저를 명시적으로 등록 (비번은 bcrypt로 인코딩됨)
    @Bean
    fun adminUsers(pe: PasswordEncoder) =
        MapReactiveUserDetailsService(
            User.withUsername("actuator")
                .password(pe.encode("actuator-pass"))
                .roles("ADMIN")
                .build()
        )

    @Bean
    @Order(0)
    fun actuatorChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/actuator/**"))
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/health", "/actuator/info").permitAll()
                it.anyExchange().hasRole("ADMIN")
            }
            .httpBasic { } // Basic은 여기서만
            .build()

    @Bean
    @Order(1)
    fun apiChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/api/auth/**").permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterAt(jwtWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
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