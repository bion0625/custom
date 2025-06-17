package com.self_true.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {
    private final static long expirationMs = 1000 * 60 * 60; // 1시간

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String username, List<String> roles) {
        // ADMIN -> ROLE_ADMIN
        List<String> prefixedRoles = roles.stream()
                .map(role -> "ROLE_" + role)
                .toList();
        return Jwts.builder()
                .setClaims(Map.of("roles", prefixedRoles))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }
}

