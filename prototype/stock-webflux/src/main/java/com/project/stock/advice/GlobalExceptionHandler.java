package com.project.stock.advice;

import com.project.stock.domain.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// GlobalExceptionHandler
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArg(IllegalArgumentException ex) {
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "conflict";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(msg));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException ex) {
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Invalid Credentials";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(msg));
    }
}
