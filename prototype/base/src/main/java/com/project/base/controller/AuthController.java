package com.project.base.controller;

import com.project.base.domain.dto.LoginRequest;
import com.project.base.domain.dto.SignUpRequest;
import com.project.base.domain.dto.TokenResponse;
import com.project.base.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입: 201 Created, 바디 없음
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> signUp(@RequestBody SignUpRequest req) {
        return userService.signUp(req);
    }

    // 로그인: TokenResponse 반환
    @PostMapping("/login")
    public Mono<TokenResponse> login(@RequestBody LoginRequest req) {
        return userService.login(req.username(), req.password());
    }
}
