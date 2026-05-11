package com.example.ctu.service;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ctu.config.AppProperties;
import com.example.ctu.exception.BadRequestException;

@Service
public class RedisOtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisOtpService.class);
    private final StringRedisTemplate redis;
    private final AppProperties properties;

    public RedisOtpService(StringRedisTemplate redis, AppProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    private String keyFor(String email) {
        return "OTP:" + email.toLowerCase().trim();
    }

    public void saveOtp(String email, String otp) {
        String key = keyFor(email);
        long ttlSeconds = properties.otp().ttlMinutes() * 60;
        redis.opsForValue().set(key, otp, Duration.ofSeconds(ttlSeconds));
        LOGGER.info("Saved OTP to redis for {} (ttl={}s)", email, ttlSeconds);
    }

    public boolean verify(String email, String otp) {
        String key = keyFor(email);
        String saved = redis.opsForValue().get(key);
        if (saved == null) {
            throw new BadRequestException("OTP đã hết hạn");
        }
        if (!Objects.equals(saved, otp)) {
            throw new BadRequestException("OTP không hợp lệ");
        }
        // consume
        redis.delete(key);
        LOGGER.info("OTP verified and removed from redis for {}", email);
        return true;
    }

    public void delete(String email) {
        redis.delete(keyFor(email));
    }
}
