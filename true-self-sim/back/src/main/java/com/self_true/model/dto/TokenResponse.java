package com.self_true.model.dto;

import lombok.Data;

@Data
public class TokenResponse extends Response {
    private String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
        setIsSuccess(true);
    }
}
