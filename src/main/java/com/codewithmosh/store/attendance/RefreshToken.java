package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", schema = "store_api")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "revoked")
    private Boolean revoked;

    @Column(name = "created_at")
    private Instant createdAt;

    public boolean isRevoked() {
        return revoked;
    }
}