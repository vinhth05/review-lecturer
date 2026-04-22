package com.example.ctu.service;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.UserRepository;
import com.example.ctu.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                       FacultyRepository facultyRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public String register(AuthDtos.RegisterRequest request) {
        if (!request.email().endsWith("@student.ctu.edu.vn")) {
            throw new BadRequestException("Email phải thuộc domain @student.ctu.edu.vn");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại");
        }
        if (userRepository.existsByStudentCode(request.studentCode())) {
            throw new BadRequestException("Mã số sinh viên đã tồn tại");
        }
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa không tồn tại"));
        User user = User.builder()
                .studentCode(request.studentCode())
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .faculty(faculty)
                .role(Role.STUDENT)
                .verified(false)
                .build();
        userRepository.save(user);
        otpService.generateAndSend(user.getEmail());
        return "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP.";
    }

    @Transactional
    public AuthDtos.AuthResponse verify(AuthDtos.VerifyRequest request) {
        otpService.verify(request.email(), request.otp());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        user.setVerified(true);
        userRepository.save(user);
        return createAuthResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Thông tin đăng nhập không hợp lệ"));
        if (!user.isVerified()) {
            throw new BadRequestException("Tài khoản chưa được xác thực OTP");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Thông tin đăng nhập không hợp lệ");
        }
        return createAuthResponse(user);
    }

    private AuthDtos.AuthResponse createAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token, user.getRole(), user.isVerified(), user.getFullName(), user.getFaculty().getName());
    }
}
