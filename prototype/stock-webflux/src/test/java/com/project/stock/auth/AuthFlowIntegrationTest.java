package com.project.stock.auth;

import com.project.stock.domain.dto.LoginRequest;
import com.project.stock.domain.dto.SignUpRequest;
import com.project.stock.domain.dto.TokenResponse;
import com.project.stock.repository.AppUserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    WebTestClient client;

    @Autowired
    AppUserRepository userRepo;

    @BeforeEach
    void clean() {
        // 필요 시 DB 초기화 (테스트마다 테이블 비우기)
        userRepo.deleteAll().block();
    }

    @Test
    void fromSignUpToLoginToMe() {
        String username = "user_" + UUID.randomUUID();
        String password = "pw1234";
        String email = "test@test.com";
        String phoneNumber = "01012341234";

        // 1) 회원가입
        client.post().uri("/api/auth/signup")
                .bodyValue(new SignUpRequest(username, password, email, phoneNumber))
                .exchange()
                .expectStatus().isCreated();

        // 2) 로그인 -> 토큰 확보
        TokenResponse tokenResponse = client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(tokenResponse).isNotNull();
        String token = tokenResponse.accessToken();
        Assertions.assertThat(token).isNotBlank();

        // 3) 보호 API 접근
        client.get().uri("/api/me")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.principal").isEqualTo(username);
    }

    @Test
    void wrongPasswordReturns401() {
        String username = "user_" + UUID.randomUUID();
        String password = "pw1234";
        String email = "test@test.com";
        String phoneNumber = "01012341234";

        client.post().uri("/api/auth/signup")
                .bodyValue(new SignUpRequest(username, password, email, phoneNumber))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(username, "wrongpass"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void duplicateUsernameReturns409() {
        String username = "dup_" + UUID.randomUUID();

        client.post().uri("/api/auth/signup")
                .bodyValue(new SignUpRequest(username, "pw", "test@test.com", "01012341234"))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/api/auth/signup")
                .bodyValue(new SignUpRequest(username, "pw2", "test@test.com", "01012341234"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void callingProtectedApiWithoutTokenReturns401() {
        client.get().uri("/api/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
