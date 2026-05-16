package com.example.ctu.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.config.AppProperties;
import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AppProperties appProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        Instant expiresAt = Instant.now().plus(appProperties.jwt().refreshTokenExpirationDays(), ChronoUnit.DAYS);
        
        // Revoke previous tokens for this user to enable token rotation
        refreshTokenRepository.findByUserAndRevokedFalse(user).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
        
        return refreshTokenRepository.save(RefreshToken.generate(user, expiresAt));
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không tồn tại"));

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Revoke old token
        refreshToken.setRevoked(true);
        refreshToken.setRotatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        // Generate new token
        User user = refreshToken.getUser();
        Instant expiresAt = Instant.now().plus(appProperties.jwt().refreshTokenExpirationDays(), ChronoUnit.DAYS);
        return refreshTokenRepository.save(RefreshToken.generate(user, expiresAt));
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không tồn tại"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không tồn tại"));
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
