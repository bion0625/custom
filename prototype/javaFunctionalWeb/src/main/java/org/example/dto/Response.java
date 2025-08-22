package org.example.dto;

import java.util.Map;

public record Response(
        int status,
        Map<String, String> headers,
        String body) {

    public static Response of(int status, String body) {
        return new Response(status, Map.of("Content-Type", "text/plain; charset=utf-8"), body);
    }
}
