package com.example.ctu.repository;

import com.example.ctu.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findTopByEmailAndTokenAndUsedFalseOrderByCreatedAtDesc(String email, String token);
    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
