package com.self_true.service;

import com.self_true.exception.NotFoundMemberException;
import com.self_true.model.dto.request.LoginRequest;
import com.self_true.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberService memberService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }
    public String getTokenByLogin(LoginRequest request) {
        return memberService.findById(request.getId())
                .filter(member -> passwordEncoder.matches(request.getPassword(), member.getPassword()))
                .map(member -> jwtUtil.generateToken(member.getMemberId(), List.of(member.getRole())))
                .orElseThrow(() -> new NotFoundMemberException("member not found"));
    }
}
