package com.stxtory.lietzsche.controller

import com.stxtory.lietzsche.dto.LoginRequest
import com.stxtory.lietzsche.dto.SignUpRequest
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestBody req: SignUpRequest): Mono<Void> =
        userService.signUp(req.username, req.password).then()

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): Mono<TokenResponse> =
        userService.login(req.username, req.password)
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentials : RuntimeException("invalid credentials")