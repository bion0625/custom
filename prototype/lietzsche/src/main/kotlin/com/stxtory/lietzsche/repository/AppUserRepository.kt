package com.stxtory.lietzsche.repository

import com.stxtory.lietzsche.domain.AppUser
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AppUserRepository : CoroutineCrudRepository<AppUser, Long> {
    suspend fun findByUsername(username: String): AppUser?
    suspend fun existsByUsername(username: String): Boolean
}