package com.self_true.exception;

import com.self_true.model.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateMemberIdException.class)
    public ResponseEntity<Response> handleDuplicateMemberIdException(DuplicateMemberIdException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new Response(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGeneralException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response(false, "서버 오류: " + e.getMessage()));
    }
}
