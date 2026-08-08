package com.example.ctu.otp;

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

@ExtendWith(MockitoExtension.class)
class OtpControllerTest {

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
    void sendOtp_ReturnsApiResponseSuccess() throws Exception {
        doNothing().when(otpService).sendOtp("test@student.ctu.edu.vn");

        mockMvc.perform(post("/auth/send-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"test@student.ctu.edu.vn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mã OTP đã được gửi thành công"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(otpService).sendOtp("test@student.ctu.edu.vn");
    }

    @Test
    void verifyOtp_ReturnsApiResponseWithVerifiedStatus() throws Exception {
        when(otpService.verifyOtp("test@student.ctu.edu.vn", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/verify-otp")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"test@student.ctu.edu.vn\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xác thực OTP thành công"))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(otpService).verifyOtp("test@student.ctu.edu.vn", "123456");
    }
}
