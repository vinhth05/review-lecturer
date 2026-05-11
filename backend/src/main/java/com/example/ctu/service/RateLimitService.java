package com.example.ctu.service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ctu.exception.BadRequestException;

/**
 * Service để rate-limit OTP requests.
 * Ngăn chặn spam bằng cách giới hạn số lần request OTP trong một khoảng thời gian.
 */
@Service
public class RateLimitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitService.class);
    
    // Map lưu lần gửi OTP cuối cùng cho mỗi email
    private final Map<String, Instant> otpLastSentTime = Collections.synchronizedMap(new HashMap<>());
    
    private final int rateLimitSeconds;
    
    public RateLimitService(@Value("${app.otp.rate-limit-seconds:60}") int rateLimitSeconds) {
        this.rateLimitSeconds = rateLimitSeconds;
    }
    
    /**
     * Kiểm tra xem có thể gửi OTP cho email này không.
     * 
     * @param email Email cần kiểm tra
     * @throws BadRequestException nếu trong thời gian chờ
     */
    public void checkRateLimit(String email) {
        Instant lastSent = otpLastSentTime.get(email);
        if (lastSent == null) {
            return; // Lần đầu tiên, cho phép
        }
        
        Instant now = Instant.now();
        long secondsElapsed = java.time.temporal.ChronoUnit.SECONDS.between(lastSent, now);
        
        if (secondsElapsed < rateLimitSeconds) {
            long secondsRemaining = rateLimitSeconds - secondsElapsed;
            LOGGER.warn("Rate limit exceeded for email: {}. Must wait {} more seconds.", email, secondsRemaining);
            throw new BadRequestException(
                String.format("Vui lòng đợi %d giây trước khi gửi OTP lần tiếp theo.", secondsRemaining)
            );
        }
    }
    
    /**
     * Ghi nhận lần gửi OTP cho email này.
     * 
     * @param email Email đã gửi OTP
     */
    public void recordOtpSent(String email) {
        otpLastSentTime.put(email, Instant.now());
        LOGGER.info("OTP send recorded for email: {}", email);
    }
    
    /**
     * Xoá lần gửi OTP (sử dụng khi xác thực thành công).
     * 
     * @param email Email cần xoá
     */
    public void clearRateLimit(String email) {
        otpLastSentTime.remove(email);
        LOGGER.info("Rate limit cleared for email: {}", email);
    }
}
