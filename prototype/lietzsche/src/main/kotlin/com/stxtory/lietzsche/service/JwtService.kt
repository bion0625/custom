package com.stxtory.lietzsche.service

import com.stxtory.lietzsche.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class JwtService(private val props: JwtProperties) {

    private val key = Keys.hmacShaKeyFor(props.secret.toByteArray())

    fun generate(username: String, roles: List<String>): String {
        val now = Instant.now()
        val exp = now.plusSeconds(props.accessTokenTtlSeconds)
        return Jwts.builder()
            .setSubject(username)
            .setIssuer(props.issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .claim("roles", roles)
            .signWith(key)
            .compact()
    }

    fun parseUsername(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject

    fun parseRoles(token: String): List<String> =
        (Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body["roles"] as List<*>)
            .map { it.toString() }
}