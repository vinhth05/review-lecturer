package com.example.ctu.otp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.common.ApiResponse;
import com.example.ctu.otp.dto.OtpSendRequest;
import com.example.ctu.otp.dto.OtpVerifyRequest;
import com.example.ctu.otp.dto.OtpVerifyResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        otpService.sendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi thành công", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.ok(ApiResponse.success("Xác thực OTP thành công", new OtpVerifyResponse(verified)));
    }
}
