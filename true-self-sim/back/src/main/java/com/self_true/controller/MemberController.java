package com.self_true.controller;

import com.self_true.model.dto.response.Response;
import com.self_true.model.dto.request.MembersRequest;
import com.self_true.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "MemberController", description = "사용자 API")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원가입", description = "회원가입 요청을 처리")
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody MembersRequest request) {
        memberService.register(request);
        return ResponseEntity.ok(new Response(true, "회원가입 완료"));
    }
}
