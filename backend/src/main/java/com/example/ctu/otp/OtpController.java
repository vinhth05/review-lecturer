package com.example.ctu.otp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.otp.dto.OtpSendRequest;
import com.example.ctu.otp.dto.OtpVerifyRequest;
import com.example.ctu.otp.dto.OtpVerifyResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        otpService.sendOtp(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(new OtpVerifyResponse(otpService.verifyOtp(request.email(), request.otp())));
    }
}
