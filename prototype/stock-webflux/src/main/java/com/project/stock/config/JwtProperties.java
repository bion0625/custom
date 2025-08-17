package com.project.stock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    String issuer;
    String secret;
    Long accessTokenTtlSeconds;
}
