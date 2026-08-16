package com.example.ctu.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.config.AppProperties;
import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.exception.UnauthorizedException;
import com.example.ctu.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    /** The raw token only exists in this short-lived return value. */
    public record RefreshTokenGrant(RefreshToken refreshToken, String token) {
    }

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AppProperties appProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public RefreshTokenGrant createRefreshToken(User user) {
        // This project intentionally allows a single refresh session per user.
        refreshTokenRepository.revokeAllActiveByUser(user);
        return issue(user);
    }

    @Transactional
    public RefreshTokenGrant rotateRefreshToken(String oldToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(hash(oldToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token does not exist"));

        if (!refreshToken.isValid()) {
            if (refreshToken.isRevoked() && refreshToken.getRotatedAt() != null) {
                // Reuse of an already rotated token indicates replay or a lost race.
                refreshTokenRepository.revokeAllActiveByUser(refreshToken.getUser());
            }
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        User user = refreshToken.getUser();
        if (user.isLocked() || !user.isVerified()) {
            refreshTokenRepository.revokeAllActiveByUser(user);
            throw new UnauthorizedException("Account is not allowed to refresh this session");
        }

        refreshToken.setRevoked(true);
        refreshToken.setRotatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
        return issue(user);
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(hash(token))
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token does not exist"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token does not exist"));
    }

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllActiveByUser(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private RefreshTokenGrant issue(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plus(
                appProperties.jwt().refreshTokenExpirationDays(), ChronoUnit.DAYS);
        RefreshToken stored = refreshTokenRepository.save(
                RefreshToken.generate(user, expiresAt, hash(rawToken)));
        return new RefreshTokenGrant(stored, rawToken);
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Refresh token is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
