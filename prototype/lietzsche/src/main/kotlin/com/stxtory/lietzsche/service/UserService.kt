package com.stxtory.lietzsche.service

import com.stxtory.lietzsche.controller.InvalidCredentials
import com.stxtory.lietzsche.domain.AppUser
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.repository.AppUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repo: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    suspend fun signUp(username: String, rawPassword: String) {
        if (repo.existsByUsername(username)) throw IllegalArgumentException("username already exists")
        val enc = withContext(Dispatchers.Default) { passwordEncoder.encode(rawPassword) } // CPU 무거움
        repo.save(AppUser(username = username, password = enc, roles = "ROLE_USER"))
    }

    suspend fun login(username: String, rawPassword: String): TokenResponse {
        val user = repo.findByUsername(username) ?: throw InvalidCredentials()
        val ok = withContext(Dispatchers.Default) { passwordEncoder.matches(rawPassword, user.password) }
        if (!ok) throw InvalidCredentials()
        val roles = user.roles.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return TokenResponse(jwtService.generate(username = user.username, roles = roles))
    }
}