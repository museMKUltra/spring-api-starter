package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.JwtConfig;
import com.codewithmosh.store.users.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final JwtConfig jwtConfig;

    public RefreshToken create(User user, String tokenValue) {
        var now = Instant.now();

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(now.plusSeconds(jwtConfig.getRefreshTokenExpiration()))
                .revoked(false)
                .createdAt(now)
                .build();

        return repository.save(token);
    }

    public RefreshToken verify(String tokenValue) {
        RefreshToken token = repository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    public void revoke(String tokenValue) {
        repository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }

    public void delete(String tokenValue) {
        repository.deleteByToken(tokenValue);
    }

    public void deleteAllByUser(Long userId) {
        repository.deleteByUserId(userId);
    }
}
