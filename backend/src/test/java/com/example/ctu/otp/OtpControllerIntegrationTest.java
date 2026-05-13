package com.example.ctu.otp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;

@SpringBootTest
@AutoConfigureMockMvc
class OtpControllerIntegrationTest {

    private static final String OTP_PREFIX = "otp:";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redis;

    @AfterEach
    @SuppressWarnings("unused")
    void cleanup() {
        redis.delete(OTP_PREFIX + "otp-integration@example.com");
        redis.delete(OTP_PREFIX + "verify-integration@example.com");
        redis.delete(OTP_PREFIX + "invalid-integration@example.com");
    }

    @Test
    void sendOtpStoresCodeInRedis() throws Exception {
        var json = Objects.requireNonNull(APPLICATION_JSON);
        mockMvc.perform(post("/api/auth/send-otp")
            .contentType(json)
                .content("{\"email\":\"otp-integration@example.com\"}"))
            .andExpect(status().isAccepted());

        String otp = redis.opsForValue().get(OTP_PREFIX + "otp-integration@example.com");
        assertThat(otp).isNotNull();
        assertThat(otp).matches("\\d{6}");
    }

    @Test
    void verifyOtpClearsRedisKey() throws Exception {
        redis.opsForValue().set(OTP_PREFIX + "verify-integration@example.com", "123456", Objects.requireNonNull(Duration.ofMinutes(5)));

        var json = Objects.requireNonNull(APPLICATION_JSON);
        mockMvc.perform(post("/api/auth/verify-otp")
            .contentType(json)
                .content("{\"email\":\"verify-integration@example.com\",\"otp\":\"123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true));

        String otp = redis.opsForValue().get(OTP_PREFIX + "verify-integration@example.com");
        assertThat(otp).isNull();
    }

    @Test
    void verifyOtpRejectsInvalidCode() throws Exception {
        redis.opsForValue().set(OTP_PREFIX + "invalid-integration@example.com", "123456", Objects.requireNonNull(Duration.ofMinutes(5)));

        var json = Objects.requireNonNull(APPLICATION_JSON);
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(json)
                .content("{\"email\":\"invalid-integration@example.com\",\"otp\":\"000000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("OTP expired or invalid"));
    }

    @TestConfiguration
    @SuppressWarnings("unused")
    static class TestOtpConfig {

        @Bean
        @Primary
        @SuppressWarnings("unused")
        OtpKafkaProducer otpKafkaProducer() {
            return mock(OtpKafkaProducer.class);
        }
    }
}
