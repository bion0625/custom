package com.stxtory.lietzsche.repository

import com.stxtory.lietzsche.domain.AppUser
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface AppUserRepository : ReactiveCrudRepository<AppUser, Long> {
    fun findByUsername(username: String): Mono<AppUser>
    fun existsByUsername(username: String): Mono<Boolean>
}