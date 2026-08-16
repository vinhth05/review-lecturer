package com.example.ctu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token join fetch token.user where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    
    List<RefreshToken> findByUserAndRevokedFalse(User user);
    
    void deleteByUserAndRevokedTrue(User user);
    
    void deleteByExpiresAtBefore(java.time.Instant expiresAt);

    @Modifying
    @Query("update RefreshToken token set token.revoked = true where token.user = :user and token.revoked = false")
    int revokeAllActiveByUser(@Param("user") User user);
}
