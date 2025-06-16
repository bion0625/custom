package com.self_true.service;

import com.self_true.model.dto.MembersRequest;
import com.self_true.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private JwtUtil jwtUtil;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberService memberService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }
    public String getTokenByLogin(MembersRequest request) {
        return memberService.findById(request.getId())
                .filter(member -> passwordEncoder.matches(request.getPassword(), member.getPassword()))
                .map(member -> jwtUtil.generateToken(request.getId()))
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
    }
}
