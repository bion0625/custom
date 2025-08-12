package com.stxtory.lietzsche.controller

import com.stxtory.lietzsche.dto.LoginRequest
import com.stxtory.lietzsche.dto.SignUpRequest
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.service.JwtService
import com.stxtory.lietzsche.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestBody req: SignUpRequest): Mono<Void> =
        userService.signUp(req.username, req.password).then()

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): Mono<TokenResponse> =
        userService.findByUsername(req.username).flatMap { user ->
            if (passwordEncoder.matches(req.password, user.password)) {
                val token = jwtService.generate(
                    username = user.username,
                    roles = user.roles.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                )
                Mono.just(TokenResponse(token))
            } else {
                Mono.error(InvalidCredentials())
            }
        }.switchIfEmpty(Mono.error(InvalidCredentials()))
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentials : RuntimeException("invalid credentials")