package com.example.ctu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.UserRepository;
import com.example.ctu.security.JwtService;

@Service
@SuppressWarnings("null")
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;

    private final String adminSeedEmail;
    private final String adminSeedPassword;
    private final String adminSeedStudentCode;
    private final String adminSeedFullName;
    private final String adminSeedFacultyCode;

    private final String studentSeedEmail;
    private final String studentSeedPassword;
    private final String studentSeedStudentCode;
    private final String studentSeedFullName;
    private final String studentSeedFacultyCode;

    private final String superAdminSeedEmail;
    private final String superAdminSeedPassword;
    private final String superAdminSeedStudentCode;
    private final String superAdminSeedFullName;
    private final String superAdminSeedFacultyCode;

    public AuthService(UserRepository userRepository,
                       FacultyRepository facultyRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       OtpService otpService,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.admin.email:admin@ctu.edu.vn}") String adminSeedEmail,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.admin.password:Admin@123}") String adminSeedPassword,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.admin.student-code:AD0001}") String adminSeedStudentCode,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.admin.full-name:Quan tri vien CTU}") String adminSeedFullName,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.admin.faculty-code:ICT}") String adminSeedFacultyCode,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.student.email:student01@student.ctu.edu.vn}") String studentSeedEmail,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.student.password:Student@123}") String studentSeedPassword,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.student.student-code:SV0001}") String studentSeedStudentCode,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.student.full-name:Sinh vien Demo}") String studentSeedFullName,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.student.faculty-code:ECO}") String studentSeedFacultyCode,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.super-admin.email:superadmin@ctu.edu.vn}") String superAdminSeedEmail,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.super-admin.password:Super@123}") String superAdminSeedPassword,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.super-admin.student-code:SA0001}") String superAdminSeedStudentCode,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.super-admin.full-name:Super Admin CTU}") String superAdminSeedFullName,
                       @org.springframework.beans.factory.annotation.Value("${app.seed.accounts.super-admin.faculty-code:ICT}") String superAdminSeedFacultyCode) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.adminSeedEmail = adminSeedEmail;
        this.adminSeedPassword = adminSeedPassword;
        this.adminSeedStudentCode = adminSeedStudentCode;
        this.adminSeedFullName = adminSeedFullName;
        this.adminSeedFacultyCode = adminSeedFacultyCode;
        this.studentSeedEmail = studentSeedEmail;
        this.studentSeedPassword = studentSeedPassword;
        this.studentSeedStudentCode = studentSeedStudentCode;
        this.studentSeedFullName = studentSeedFullName;
        this.studentSeedFacultyCode = studentSeedFacultyCode;
        this.superAdminSeedEmail = superAdminSeedEmail;
        this.superAdminSeedPassword = superAdminSeedPassword;
        this.superAdminSeedStudentCode = superAdminSeedStudentCode;
        this.superAdminSeedFullName = superAdminSeedFullName;
        this.superAdminSeedFacultyCode = superAdminSeedFacultyCode;
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
        LOGGER.info("Login attempt for email: {}", request.email());
        User seedUser = authenticateSeedAccount(request.email(), request.password());
        if (seedUser != null) {
            LOGGER.info("Seed account login succeeded for: {}", request.email());
            return createAuthResponse(seedUser);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            LOGGER.info("Authentication succeeded for: {}", request.email());
        } catch (AuthenticationException exception) {
            LOGGER.warn("Authentication failed for {}: {}", request.email(), exception.getMessage());
            throw new BadRequestException("Thông tin đăng nhập không hợp lệ");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Thông tin đăng nhập không hợp lệ"));
        if (!user.isVerified()) {
            LOGGER.warn("User {} not verified", request.email());
            throw new BadRequestException("Tài khoản chưa được xác thực OTP");
        }
        LOGGER.info("Login successful for: {}", request.email());
        return createAuthResponse(user);
    }

    @Transactional
    private User authenticateSeedAccount(String email, String password) {
        SeedAccount seedAccount = resolveSeedAccount(email);
        if (seedAccount == null || !seedAccount.password().equals(password)) {
            return null;
        }

        Faculty faculty = facultyRepository.findByCode(seedAccount.facultyCode())
                .orElseGet(() -> facultyRepository.save(Faculty.builder()
                        .code(seedAccount.facultyCode())
                        .name(seedAccount.facultyName())
                        .build()));

        User user = userRepository.findByEmail(seedAccount.email())
                .or(() -> userRepository.findByStudentCode(seedAccount.studentCode()))
                .orElseGet(() -> User.builder().email(seedAccount.email()).studentCode(seedAccount.studentCode()).build());

        user.setStudentCode(seedAccount.studentCode());
        user.setFullName(seedAccount.fullName());
        user.setEmail(seedAccount.email());
        user.setRole(seedAccount.role());
        user.setFaculty(faculty);
        user.setVerified(true);
        user.setPasswordHash(passwordEncoder.encode(seedAccount.password()));
        return userRepository.save(user);
    }

    private SeedAccount resolveSeedAccount(String email) {
        if (adminSeedEmail.equalsIgnoreCase(email)) {
            return new SeedAccount(adminSeedEmail, adminSeedPassword, adminSeedStudentCode, adminSeedFullName, adminSeedFacultyCode, "Trường Công nghệ Thông tin & Truyền thông", Role.ADMIN);
        }
        if (studentSeedEmail.equalsIgnoreCase(email)) {
            return new SeedAccount(studentSeedEmail, studentSeedPassword, studentSeedStudentCode, studentSeedFullName, studentSeedFacultyCode, "Trường Kinh tế", Role.STUDENT);
        }
        if (superAdminSeedEmail.equalsIgnoreCase(email)) {
            return new SeedAccount(superAdminSeedEmail, superAdminSeedPassword, superAdminSeedStudentCode, superAdminSeedFullName, superAdminSeedFacultyCode, "Trường Công nghệ Thông tin & Truyền thông", Role.SUPER_ADMIN);
        }
        return null;
    }

    private record SeedAccount(
            String email,
            String password,
            String studentCode,
            String fullName,
            String facultyCode,
            String facultyName,
            Role role
    ) {
    }

    private AuthDtos.AuthResponse createAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token, user.getRole(), user.isVerified(), user.getFullName(), user.getFaculty().getName());
    }
}
