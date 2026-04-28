package com.codewithmosh.store.auth;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {
    private String secret;
    private int accessTokenExpiration;
    private int refreshTokenExpiration;
    private int guestRefreshTokenExpiration;

    public SecretKey getSecret() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public int getRefreshTokenExpiration(boolean isGuest) {
        return isGuest ? guestRefreshTokenExpiration : refreshTokenExpiration;
    }
}
