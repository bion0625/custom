package com.stxtory.lietzsche.dto

data class SignUpRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val accessToken: String, val tokenType: String = "Bearer")