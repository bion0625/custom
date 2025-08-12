package com.stxtory.lietzsche.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class AppUser(
    @Id val id: Long? = null,
    val username: String,
    val password: String,           // BCrypt 해시 저장
    val roles: String = "ROLE_USER" // 콤마구분: "ROLE_USER,ROLE_ADMIN"
)