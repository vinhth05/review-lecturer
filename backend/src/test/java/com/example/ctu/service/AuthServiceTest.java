package com.example.ctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ctu.config.AppProperties;
import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.PendingRegistration;
import com.example.ctu.otp.OtpService;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.PendingRegistrationRepository;
import com.example.ctu.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FacultyRepository facultyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OtpService otpService;
    @Mock private PendingRegistrationRepository pendingRegistrationRepository;
    @Mock private AppProperties properties;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTemporarily_deletesExistingPendingRegistrations() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "password", 1L
        );

        Faculty faculty = Faculty.builder().id(1L).code("FIT").name("FIT").build();

        // AppProperties mock
        AppProperties.Otp otpProps = new AppProperties.Otp(5L, "CTU Review Platform", "Your OTP Code");
        when(properties.otp()).thenReturn(otpProps);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByStudentCode(request.studentCode())).thenReturn(false);
        when(facultyRepository.findById(request.facultyId())).thenReturn(Optional.of(faculty));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        // Act
        String result = authService.registerTemporarily(request);

        // Assert
        verify(pendingRegistrationRepository).deleteByEmail(request.email());
        verify(pendingRegistrationRepository).deleteByStudentCode(request.studentCode());
        verify(pendingRegistrationRepository).save(any(PendingRegistration.class));
        verify(otpService).sendOtp(request.email());
        assertThat(result).contains("Đăng ký thành công");
    }
}
