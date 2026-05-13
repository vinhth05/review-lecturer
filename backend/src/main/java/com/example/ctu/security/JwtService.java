package com.example.ctu.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.ctu.config.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public String generateToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("userId", userId, "email", email, "role", role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.jwt().expirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(getKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        String secret = properties.jwt().secret();
        byte[] keyBytes;

        if (secret.startsWith("base64:")) {
            keyBytes = Base64.getDecoder().decode(secret.substring("base64:".length()));
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                try {
                    keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
                } catch (NoSuchAlgorithmException exception) {
                    throw new IllegalStateException("Unable to derive JWT signing key", exception);
                }
            }
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
