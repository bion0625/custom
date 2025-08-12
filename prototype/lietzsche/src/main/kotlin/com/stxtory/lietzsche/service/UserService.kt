package com.stxtory.lietzsche.service

import com.stxtory.lietzsche.controller.InvalidCredentials
import com.stxtory.lietzsche.domain.AppUser
import com.stxtory.lietzsche.dto.TokenResponse
import com.stxtory.lietzsche.repository.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class UserService(
    private val repo: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun signUp(username: String, rawPassword: String): Mono<AppUser> =
        repo.existsByUsername(username).flatMap { exists ->
            if (exists) Mono.error(IllegalArgumentException("username already exists"))
            else Mono.fromCallable { passwordEncoder.encode(rawPassword) }       // BCrypt는 offload
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap { enc ->
                    repo.save(AppUser(username = username, password = enc, roles = "ROLE_USER"))
                }
        }

    fun login(username: String, rawPassword: String): Mono<TokenResponse> =
        repo.findByUsername(username).flatMap { user ->
            Mono.fromCallable { passwordEncoder.matches(rawPassword, user.password) } // offload
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap { ok ->
                    if (!ok) Mono.error(InvalidCredentials())
                    else {
                        val roles = user.roles.split(",").map(String::trim).filter(String::isNotEmpty)
                        Mono.just(TokenResponse(jwtService.generate(user.username, roles)))
                    }
                }
        }.switchIfEmpty(Mono.error(InvalidCredentials()))
}