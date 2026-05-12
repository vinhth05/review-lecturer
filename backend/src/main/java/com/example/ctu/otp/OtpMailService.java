package com.example.ctu.otp;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.ctu.otp.dto.OtpEmailMessage;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@SuppressWarnings("null")
public class OtpMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpMailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String mailFrom;
    private final String subject;

    public OtpMailService(JavaMailSender mailSender,
                          TemplateEngine templateEngine,
                          @Value("${spring.mail.username}") String mailFrom,
                          @Value("${app.otp.mail-subject:Your OTP Code}") String subject) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mailFrom = mailFrom;
        this.subject = subject;
    }

    @Async
    public void sendOtpEmail(OtpEmailMessage message) {
        Context context = new Context();
        context.setVariable("email", message.email());
        context.setVariable("otp", message.otp());
        context.setVariable("appName", message.appName());
        context.setVariable("expiresInMinutes", getRemainingMinutes(message.expiresAt()));

        String content = templateEngine.process("mail/otp-email", context);
        sendHtml(message.email(), subject, content);
    }

    private int getRemainingMinutes(Instant expiresAt) {
        long minutes = Duration.between(Instant.now(), expiresAt).toMinutes();
        return (int) Math.max(1, minutes);
    }

    private void sendHtml(String to, String subject, String html) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(mailFrom);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mimeMessage);
            LOGGER.info("OTP email sent to {}", to);
        } catch (MailException | MessagingException exception) {
            LOGGER.error("Failed to send OTP email to {}", to, exception);
        }
    }
}
