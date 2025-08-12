package com.stxtory.lietzsche.controller

import com.stxtory.lietzsche.dto.LoginRequest
import com.stxtory.lietzsche.dto.SignUpRequest
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
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
    suspend fun signUp(@RequestBody req: SignUpRequest) {
        userService.signUp(req.username, req.password)
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody req: LoginRequest): TokenResponse =
        userService.login(req.username, req.password)
}

@RestController
class MeController {
    @GetMapping("/api/me")
    suspend fun me(@AuthenticationPrincipal principal: Any?): Map<String, Any?> =
        mapOf("principal" to principal)
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentials : RuntimeException("invalid credentials")