package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public class Jwt {
    private final Claims claims;
    private final SecretKey secretKey;

    public Jwt(Claims claims, SecretKey secretKey) {
        this.claims = claims;
        this.secretKey = secretKey;
    }

    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    public Long getUserId() {
        return Long.valueOf(claims.getSubject());
    }

    public Role getRole() {
        return Role.valueOf(claims.get("role", String.class));
    }

    public boolean isGuest() {
        return Boolean.TRUE.equals(claims.get("isGuest", Boolean.class));
    }

    public Long getExpiresAt() {
        return claims.get("expiresAt", Long.class);
    }

    public boolean isGuestExpired() {
        var expiresAt = getExpiresAt();

        System.out.println(System.currentTimeMillis());

        return isGuest()
                && expiresAt != null
                && expiresAt < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return Jwts.builder().claims(claims).signWith(secretKey).compact();
    }
}
