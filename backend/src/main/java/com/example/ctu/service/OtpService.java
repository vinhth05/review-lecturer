package com.example.ctu.service;

import java.security.SecureRandom;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.ctu.config.AppProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class OtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpService.class);
    private final RedisOtpService redisOtpService;
    private final JavaMailSender mailSender;
    private final AppProperties properties;
    private final TemplateEngine templateEngine;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(RedisOtpService redisOtpService, JavaMailSender mailSender, AppProperties properties, TemplateEngine templateEngine) {
        this.redisOtpService = redisOtpService;
        this.mailSender = mailSender;
        this.properties = properties;
        this.templateEngine = templateEngine;
    }

    public void generateAndSend(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        LOGGER.info("Generating OTP for email: {}", email);
        // Save OTP in Redis with TTL
        redisOtpService.saveOtp(email, otp);
        sendEmail(email, otp);
    }

    public boolean verify(String email, String otp) {
        // Verify against Redis
        return redisOtpService.verify(email, otp);
    }

    @Async
    public void sendEmail(String email, String otp) {
        try {
            LOGGER.info("Sending OTP email to: {}", email);
            
            // Prepare context for Thymeleaf template
            Context context = new Context(Locale.forLanguageTag("vi"));
            context.setVariable("email", email);
            context.setVariable("otp", otp);
            context.setVariable("ttlMinutes", properties.otp().ttlMinutes());
            
            // Process template
            String htmlContent = templateEngine.process("mail/otp", context);
            
            // Send email with HTML content
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom("noreply@ctu-review.local");
            helper.setTo(email);
            helper.setSubject("CTU Review Platform - Mã OTP xác thực");
            helper.setText(htmlContent, true);  // true = HTML content
            
            mailSender.send(mimeMessage);
            LOGGER.info("OTP email sent successfully to: {}", email);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send OTP email to: {}", email, exception);
            // Don't throw exception in async method - log it instead
            LOGGER.error("OTP email sending failed permanently for: {}", email, exception);
        }
    }
}
