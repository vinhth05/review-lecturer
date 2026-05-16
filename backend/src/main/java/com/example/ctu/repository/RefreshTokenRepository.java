package com.example.ctu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUserAndRevokedFalse(User user);
    
    void deleteByUserAndRevokedTrue(User user);
    
    void deleteByExpiresAtBefore(java.time.Instant expiresAt);
}
