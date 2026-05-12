package com.example.ctu.otp;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ctu.exception.BadRequestException;
import com.example.ctu.otp.dto.OtpEmailMessage;

@Service
@SuppressWarnings("null")
public class OtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpService.class);
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String PASSWORD_RESET_TOKEN_PREFIX = "pwd-reset-token:";

    private final StringRedisTemplate redis;
    private final OtpKafkaProducer otpKafkaProducer;
    private final SecureRandom secureRandom;
    private final int ttlMinutes;
    private final String appName;

    public OtpService(StringRedisTemplate redis,
                      OtpKafkaProducer otpKafkaProducer,
                      @Value("${app.otp.ttl-minutes:5}") int ttlMinutes,
                      @Value("${app.otp.app-name:CTU Review Platform}") String appName) {
        this.redis = redis;
        this.otpKafkaProducer = otpKafkaProducer;
        this.ttlMinutes = ttlMinutes;
        this.appName = appName;
        this.secureRandom = new SecureRandom();
    }

    public void sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String key = keyFor(normalizedEmail);
        Duration ttl = Duration.ofMinutes(ttlMinutes);
        Instant expiresAt = Instant.now().plus(ttl);

        redis.opsForValue().set(key, otp, ttl);
        LOGGER.info("OTP stored in Redis for {} (ttlMinutes={})", normalizedEmail, ttlMinutes);

        otpKafkaProducer.send(new OtpEmailMessage(normalizedEmail, otp, expiresAt, appName));
    }

    public boolean verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        String key = keyFor(normalizedEmail);
        String saved = redis.opsForValue().get(key);
        if (saved == null) {
            throw new BadRequestException("OTP expired or invalid");
        }

        if (!timingSafeEquals(saved, otp)) {
            throw new BadRequestException("OTP expired or invalid");
        }

        redis.delete(key);
        LOGGER.info("OTP verified and cleared for {}", normalizedEmail);
        return true;
    }

    public String createPasswordResetToken(String email) {
        String normalizedEmail = normalizeEmail(email);
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = passwordResetTokenKey(token);
        redis.opsForValue().set(key, normalizedEmail, Duration.ofMinutes(ttlMinutes));
        LOGGER.info("Password reset token stored for {}", normalizedEmail);
        return token;
    }

    public String resolvePasswordResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Phiên xác thực OTP đã hết hạn hoặc không hợp lệ");
        }
        String email = redis.opsForValue().get(passwordResetTokenKey(token));
        if (email == null) {
            throw new BadRequestException("Phiên xác thực OTP đã hết hạn hoặc không hợp lệ");
        }
        return email;
    }

    public String consumePasswordResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Phiên xác thực OTP đã hết hạn hoặc không hợp lệ");
        }
        String key = passwordResetTokenKey(token);
        String email = redis.opsForValue().get(key);
        if (email == null) {
            throw new BadRequestException("Phiên xác thực OTP đã hết hạn hoặc không hợp lệ");
        }
        redis.delete(key);
        return email;
    }

    private String keyFor(String email) {
        return OTP_KEY_PREFIX + email;
    }

    private String passwordResetTokenKey(String token) {
        return PASSWORD_RESET_TOKEN_PREFIX + token;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean timingSafeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(), right.getBytes());
    }
}
