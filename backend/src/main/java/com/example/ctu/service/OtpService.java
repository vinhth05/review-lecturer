package com.example.ctu.service;

import com.example.ctu.config.AppProperties;
import com.example.ctu.entity.OtpToken;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.repository.OtpTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;
    private final AppProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpTokenRepository otpTokenRepository, JavaMailSender mailSender, AppProperties properties) {
        this.otpTokenRepository = otpTokenRepository;
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void generateAndSend(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .token(otp)
                .expiresAt(Instant.now().plusSeconds(properties.otp().ttlMinutes() * 60))
                .used(false)
                .build();
        otpTokenRepository.save(otpToken);
        sendEmail(email, otp);
    }

    public boolean verify(String email, String otp) {
        OtpToken token = otpTokenRepository.findTopByEmailAndTokenAndUsedFalseOrderByCreatedAtDesc(email, otp)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }
        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }

    private void sendEmail(String email, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("CTU Review Platform OTP Verification");
            helper.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong " + properties.otp().ttlMinutes() + " phút.", false);
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            throw new BadRequestException("Không thể tạo email OTP");
        }
    }
}
