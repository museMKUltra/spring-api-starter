package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);
}