package com.example.ctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.otp.OtpService;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.PendingRegistrationRepository;
import com.example.ctu.repository.UserRepository;
import com.example.ctu.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FacultyRepository facultyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OtpService otpService;
    @Mock private PendingRegistrationRepository pendingRegistrationRepository;
    @Mock private AppProperties properties;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTemporarily_success_deletesExistingPendingRegistrationsAndSendsOtp() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "password", 1L
        );

        Faculty faculty = Faculty.builder().id(1L).code("FIT").name("FIT").build();
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

    @Test
    void registerTemporarily_passwordMismatch_throwsBadRequestException() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "different-password", 1L
        );

        // Act & Assert
        assertThatThrownBy(() -> authService.registerTemporarily(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mật khẩu xác nhận không khớp");

        verify(pendingRegistrationRepository, never()).save(any());
        verify(otpService, never()).sendOtp(any());
    }

    @Test
    void registerTemporarily_emailExists_throwsBadRequestException() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "password", 1L
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerTemporarily(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email đã tồn tại");

        verify(pendingRegistrationRepository, never()).save(any());
    }

    @Test
    void registerTemporarily_studentCodeExists_throwsBadRequestException() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "password", 1L
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByStudentCode(request.studentCode())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerTemporarily(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã số sinh viên đã tồn tại");

        verify(pendingRegistrationRepository, never()).save(any());
    }

    @Test
    void registerTemporarily_facultyNotFound_throwsResourceNotFoundException() {
        // Arrange
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "SV0001", "John Doe", "john@student.ctu.edu.vn", "password", "password", 999L
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByStudentCode(request.studentCode())).thenReturn(false);
        when(facultyRepository.findById(request.facultyId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.registerTemporarily(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Khoa không tồn tại");

        verify(pendingRegistrationRepository, never()).save(any());
    }

    @Test
    void verify_existingUserFlow_success() {
        // Arrange
        AuthDtos.VerifyRequest request = new AuthDtos.VerifyRequest("john@student.ctu.edu.vn", "123456");
        User user = User.builder()
                .id(1L)
                .email("john@student.ctu.edu.vn")
                .fullName("John Doe")
                .role(Role.STUDENT)
                .faculty(Faculty.builder().name("FIT").build())
                .verified(false)
                .build();

        when(pendingRegistrationRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "john@student.ctu.edu.vn", "STUDENT")).thenReturn("mock-jwt-token");
        
        com.example.ctu.entity.RefreshToken mockRefreshToken = com.example.ctu.entity.RefreshToken.builder()
                .token("mock-refresh-token")
                .build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(mockRefreshToken);

        // Act
        AuthDtos.AuthResponse response = authService.verify(request);

        // Assert
        verify(otpService).verifyOtp(request.email(), request.otp());
        verify(userRepository).save(user);
        assertThat(user.isVerified()).isTrue();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token");
    }
}
