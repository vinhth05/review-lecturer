package com.example.ctu.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.ctu.otp.dto.OtpEmailMessage;

@Service
public class OtpKafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpKafkaConsumer.class);
    private final OtpMailService otpMailService;

    public OtpKafkaConsumer(OtpMailService otpMailService) {
        this.otpMailService = otpMailService;
    }

        @KafkaListener(
            topics = "${app.kafka.topic.otp-email}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "otpKafkaListenerContainerFactory"
        )
    public void consume(OtpEmailMessage message) {
        otpMailService.sendOtpEmail(message);
        LOGGER.info("OTP email queued for send: {}", message.email());
    }
}
