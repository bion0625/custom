package com.stxtory.lietzsche.advice

import com.stxtory.lietzsche.controller.InvalidCredentials
import org.springframework.http.*
import org.springframework.web.bind.annotation.*

data class ApiError(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArg(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError(ex.message ?: "conflict"))
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(InvalidCredentials::class)
    fun invalidCredentials(e: InvalidCredentials) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError("invalid credentials"))
}