package com.example.ctu.security;

import com.example.ctu.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public String generateToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("uid", userId.toString(), "role", role))
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
        byte[] keyBytes = properties.jwt().secret().getBytes();
        if (properties.jwt().secret().length() > 32) {
            try {
                keyBytes = Decoders.BASE64.decode(properties.jwt().secret());
            } catch (IllegalArgumentException ignored) {
                keyBytes = properties.jwt().secret().getBytes();
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
