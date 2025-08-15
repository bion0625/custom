package com.project.base.service;

import com.project.base.advice.InvalidCredentialsException;
import com.project.base.domain.AppUser;
import com.project.base.domain.dto.SignUpRequest;
import com.project.base.domain.dto.TokenResponse;
import com.project.base.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final AppUserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(AppUserRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 회원가입
     * - username 중복 검사
     * - password 인코딩 (CPU 바운드 → boundedElastic)
     * - 기본 USER 부여
     */
    public Mono<Void> signUp(SignUpRequest req) {
        return repo.existsByUsername(req.username())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("username already exists"));
                    }
                    return Mono.just(req.password());
                })
                // CPU 바운드 작업은 elastic에서
                .publishOn(Schedulers.boundedElastic())
                .map(passwordEncoder::encode)
                .flatMap(encodedPw -> {
                    AppUser user = new AppUser();
                    user.setUsername(req.username());
                    user.setPassword(encodedPw);
                    user.setEmail(req.email());
                    user.setPhoneNumber(req.phoneNumber());
                    user.setRoles("USER"); // 문자열 CSV 형태 가정
                    return repo.save(user);
                })
                .then();
    }

    /**
     * 로그인
     * - 사용자 조회
     * - 패스워드 매칭 (CPU 바운드 → boundedElastic)
     * - roles 파싱 후 JWT 발급
     */
    public Mono<TokenResponse> login(String username, String rawPassword) {
        return repo.findByUsername(username)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .publishOn(Schedulers.boundedElastic())
                .flatMap(user -> {
                    boolean ok = passwordEncoder.matches(rawPassword, user.getPassword());
                    if (!ok) {
                        return Mono.error(new InvalidCredentialsException());
                    }
                    List<String> roles = Arrays.stream(
                                    user.getRoles() != null ? user.getRoles().split(",") : new String[0]
                            )
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());

                    String token = jwtService.generate(user.getUsername(), roles);
                    return Mono.just(new TokenResponse(token));
                });
    }
}
