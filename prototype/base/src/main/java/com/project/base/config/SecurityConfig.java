package com.project.base.config;

import com.project.base.service.JwtService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import java.util.List;
import java.util.stream.Collectors;

@EnableConfigurationProperties(JwtProperties.class)
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {
    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 액추에이터용 ADMIN 유저 명시 등록 (비밀번호는 bcrypt 등으로 인코딩)
    @Bean
    public MapReactiveUserDetailsService adminUsers(PasswordEncoder pe) {
        return new MapReactiveUserDetailsService(
                User.withUsername("actuator")
                        .password(pe.encode("actuator-pass"))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    @Order(0)
    public SecurityWebFilterChain actuatorChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authz -> authz
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyExchange().hasRole("ADMIN")
                )
                // Basic 인증은 액추에이터 체인에서만 허용
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityWebFilterChain apiChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authz -> authz
                        .pathMatchers("/api/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public WebFilter jwtWebFilter() {
        return (ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring("Bearer ".length()).trim();
                try {
                    String username = jwtService.parseUsername(token);
                    List<String> roles = jwtService.parseRoles(token);
                    var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // 인증 컨텍스트를 주입한 상태로 체인 진행
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                } catch (Exception ignored) {
                    // 토큰 문제 시 비인증 상태로 통과 -> 이후 401 처리
                }
            }
            return chain.filter(exchange);
        };
    }
}
