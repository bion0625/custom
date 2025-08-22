package org.example.dto;

import java.util.List;
import java.util.Map;

public record Request(
        String method,
        String path,
        Map<String, List<String>> query,
        Map<String, String> headers,
        String body) { }
