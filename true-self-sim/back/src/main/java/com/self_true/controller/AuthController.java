package com.self_true.controller;

import com.self_true.model.dto.request.LoginRequest;
import com.self_true.model.dto.response.Response;
import com.self_true.model.dto.response.TokenResponse;
import com.self_true.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "AuthController", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "로그인", description = "인증 토큰 반환")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.getTokenByLogin(request);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Operation(summary = "상태확인")
    @GetMapping("/me")
    public ResponseEntity<Response> getMyInfo(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(new Response(true, memberId));
    }
}
