package com.stxtory.lietzsche.controller

import com.stxtory.lietzsche.dto.LoginRequest
import com.stxtory.lietzsche.dto.SignUpRequest
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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

@RestController
class MeController {
    @GetMapping("/api/me")
    fun me(@AuthenticationPrincipal principal: Any?): Mono<Map<String, Any?>> =
        Mono.just(mapOf("principal" to principal))
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentials : RuntimeException("invalid credentials")