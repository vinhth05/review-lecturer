package com.example.ctu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @Column(name = "rotated_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant rotatedAt;

    public static RefreshToken generate(User user, Instant expiresAt) {
        return RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .createdAt(Instant.now())
                .build();
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}

