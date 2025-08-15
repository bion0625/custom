package com.project.base.domain.dto;

public record TokenResponse(String accessToken, String tokenType) {

    public TokenResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
