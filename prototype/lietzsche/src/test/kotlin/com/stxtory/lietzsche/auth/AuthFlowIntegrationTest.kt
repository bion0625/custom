package com.stxtory.lietzsche.auth

import com.stxtory.lietzsche.dto.LoginRequest
import com.stxtory.lietzsche.dto.SignUpRequest
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.repository.AppUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuthFlowIntegrationTest(
    @Autowired val client: WebTestClient,
    @Autowired val userRepo: AppUserRepository
) {

    @BeforeEach
    fun clean() {
        // 필요 시 DB 초기화
        userRepo.deleteAll().block()
    }

    @Test
    fun `from 회원가입 to 로그인 to me`() {
        val username = "user_${UUID.randomUUID()}"
        val password = "pw1234"

        // 1) 회원가입
        client.post().uri("/api/auth/signup")
            .bodyValue(SignUpRequest(username, password))
            .exchange()
            .expectStatus().isCreated

        // 2) 로그인 -> 토큰 확보
        val token = client.post().uri("/api/auth/login")
            .bodyValue(LoginRequest(username, password))
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenResponse::class.java)
            .returnResult()
            .responseBody!!
            .accessToken

        assertThat(token).isNotBlank()

        // 3) 보호 API 접근
        client.get().uri("/api/me")
            .headers { it.setBearerAuth(token) }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.principal").isEqualTo(username)
    }

    @Test
    fun `비밀번호 틀리면 401`() {
        val username = "user_${UUID.randomUUID()}"
        val password = "pw1234"

        client.post().uri("/api/auth/signup")
            .bodyValue(SignUpRequest(username, password))
            .exchange()
            .expectStatus().isCreated

        client.post().uri("/api/auth/login")
            .bodyValue(LoginRequest(username, "wrongpass"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `중복 아이디면 409`() {
        val username = "dup_${UUID.randomUUID()}"

        client.post().uri("/api/auth/signup")
            .bodyValue(SignUpRequest(username, "pw"))
            .exchange()
            .expectStatus().isCreated

        client.post().uri("/api/auth/signup")
            .bodyValue(SignUpRequest(username, "pw2"))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    fun `토큰 없이 보호 API 호출하면 401`() {
        client.get().uri("/api/me")
            .exchange()
            .expectStatus().isUnauthorized
    }
}