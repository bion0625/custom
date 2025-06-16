package com.self_true.controller;

import com.self_true.model.dto.request.LoginRequest;
import com.self_true.model.dto.response.TokenResponse;
import com.self_true.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.getTokenByLogin(request);
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
