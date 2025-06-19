package com.self_true.controller;

import com.self_true.exception.NotFoundMemberException;
import com.self_true.model.dto.request.LoginRequest;
import com.self_true.model.dto.response.MytInfoResponse;
import com.self_true.model.dto.response.Response;
import com.self_true.model.dto.response.TokenResponse;
import com.self_true.service.AuthService;
import com.self_true.service.MemberService;
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
    private final MemberService memberService;

    public AuthController(AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @Operation(summary = "로그인", description = "인증 토큰 반환")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.getTokenByLogin(request);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Operation(summary = "내 정보")
    @GetMapping("/me")
    public ResponseEntity<MytInfoResponse> getMyInfo(@AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(memberService.findById(memberId)
                .map(MytInfoResponse::fromEntity)
                .orElseThrow(() -> new NotFoundMemberException("not found member id: " + memberId)));
    }
}
