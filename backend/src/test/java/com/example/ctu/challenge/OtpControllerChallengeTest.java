package com.example.ctu.challenge;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.ctu.exception.GlobalExceptionHandler;
import com.example.ctu.otp.OtpController;
import com.example.ctu.otp.OtpService;

@ExtendWith(MockitoExtension.class)
class OtpControllerChallengeTest {

    private MockMvc mockMvc;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private OtpController otpController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(otpController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testSendOtp_ValidEmail_ReturnsApiResponseVoid_Success() throws Exception {
        doNothing().when(otpService).sendOtp("student@ctu.edu.vn");

        mockMvc.perform(post("/auth/send-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"student@ctu.edu.vn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mã OTP đã được gửi thành công"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(otpService).sendOtp("student@ctu.edu.vn");
    }

    @Test
    void testSendOtp_InvalidEmail_ReturnsStatus400_ValidationError() throws Exception {
        mockMvc.perform(post("/auth/send-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"invalid-email-format\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testVerifyOtp_ValidRequest_ReturnsApiResponseOtpVerifyResponse() throws Exception {
        when(otpService.verifyOtp("student@ctu.edu.vn", "654321")).thenReturn(true);

        mockMvc.perform(post("/auth/verify-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"student@ctu.edu.vn\",\"otp\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xác thực OTP thành công"))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(otpService).verifyOtp("student@ctu.edu.vn", "654321");
    }

    @Test
    void testVerifyOtp_InvalidOtpFormat_ReturnsStatus400_ValidationError() throws Exception {
        mockMvc.perform(post("/auth/verify-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"student@ctu.edu.vn\",\"otp\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
