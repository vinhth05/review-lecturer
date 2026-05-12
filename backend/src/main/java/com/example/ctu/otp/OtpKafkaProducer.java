package com.example.ctu.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.ctu.otp.dto.OtpEmailMessage;

@Service
@SuppressWarnings("null")
public class OtpKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpKafkaProducer.class);

    private final KafkaTemplate<String, OtpEmailMessage> kafkaTemplate;
    private final String otpEmailTopic;

    public OtpKafkaProducer(KafkaTemplate<String, OtpEmailMessage> kafkaTemplate,
                            @Value("${app.kafka.topic.otp-email:otp-email-topic}") String otpEmailTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.otpEmailTopic = otpEmailTopic;
    }

    public void send(OtpEmailMessage message) {
        kafkaTemplate.send(otpEmailTopic, message.email(), message);
        LOGGER.info("OTP email message sent to Kafka topic {} for {}", otpEmailTopic, message.email());
    }
}
