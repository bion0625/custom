package com.stxtory.lietzsche.service

import com.stxtory.lietzsche.domain.AppUser
import com.stxtory.lietzsche.repository.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    private val repo: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun signUp(username: String, rawPassword: String): Mono<AppUser> =
        repo.existsByUsername(username).flatMap { exists ->
            if (exists) Mono.error(IllegalArgumentException("username already exists"))
            else repo.save(
                AppUser(
                    username = username,
                    password = passwordEncoder.encode(rawPassword),
                    roles = "ROLE_USER"
                )
            )
        }

    fun findByUsername(username: String) = repo.findByUsername(username)
}