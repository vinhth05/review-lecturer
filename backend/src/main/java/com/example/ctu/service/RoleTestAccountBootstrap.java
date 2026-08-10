package com.example.ctu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class RoleTestAccountBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleTestAccountBootstrap.class);

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminStudentCode;
    private final String adminFullName;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFacultyCode;

    private final String studentStudentCode;
    private final String studentFullName;
    private final String studentEmail;
    private final String studentPassword;
    private final String studentFacultyCode;

    private final String superAdminStudentCode;
    private final String superAdminFullName;
    private final String superAdminEmail;
    private final String superAdminPassword;
    private final String superAdminFacultyCode;

    public RoleTestAccountBootstrap(
            UserRepository userRepository,
            FacultyRepository facultyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.accounts.admin.student-code}") String adminStudentCode,
            @Value("${app.seed.accounts.admin.full-name}") String adminFullName,
            @Value("${app.seed.accounts.admin.email}") String adminEmail,
            @Value("${app.seed.accounts.admin.password}") String adminPassword,
            @Value("${app.seed.accounts.admin.faculty-code}") String adminFacultyCode,
            @Value("${app.seed.accounts.student.student-code}") String studentStudentCode,
            @Value("${app.seed.accounts.student.full-name}") String studentFullName,
            @Value("${app.seed.accounts.student.email}") String studentEmail,
            @Value("${app.seed.accounts.student.password}") String studentPassword,
            @Value("${app.seed.accounts.student.faculty-code}") String studentFacultyCode,
            @Value("${app.seed.accounts.super-admin.student-code}") String superAdminStudentCode,
            @Value("${app.seed.accounts.super-admin.full-name}") String superAdminFullName,
            @Value("${app.seed.accounts.super-admin.email}") String superAdminEmail,
            @Value("${app.seed.accounts.super-admin.password}") String superAdminPassword,
            @Value("${app.seed.accounts.super-admin.faculty-code}") String superAdminFacultyCode
    ) {
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminStudentCode = adminStudentCode;
        this.adminFullName = adminFullName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFacultyCode = adminFacultyCode;
        this.studentStudentCode = studentStudentCode;
        this.studentFullName = studentFullName;
        this.studentEmail = studentEmail;
        this.studentPassword = studentPassword;
        this.studentFacultyCode = studentFacultyCode;
        this.superAdminStudentCode = superAdminStudentCode;
        this.superAdminFullName = superAdminFullName;
        this.superAdminEmail = superAdminEmail;
        this.superAdminPassword = superAdminPassword;
        this.superAdminFacultyCode = superAdminFacultyCode;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateSeedConfiguration();
        LOGGER.info("=== Starting test account bootstrap ===");
        Faculty adminFaculty = resolveOrCreateFaculty(adminFacultyCode, "Trường Công nghệ Thông tin & Truyền thông");
        Faculty studentFaculty = resolveOrCreateFaculty(studentFacultyCode, "Trường Kinh tế");
        Faculty superAdminFaculty = resolveOrCreateFaculty(superAdminFacultyCode, "Trường Công nghệ Thông tin & Truyền thông");

        LOGGER.info("Admin faculty resolved: {}", adminFaculty.getCode());
        LOGGER.info("Student faculty resolved: {}", studentFaculty.getCode());
        LOGGER.info("SuperAdmin faculty resolved: {}", superAdminFaculty.getCode());

        LOGGER.info("Creating ADMIN bootstrap account");
        upsertAccount(adminStudentCode, adminFullName, adminEmail, adminPassword, Role.ADMIN, adminFaculty);

        LOGGER.info("Creating STUDENT bootstrap account");
        upsertAccount(studentStudentCode, studentFullName, studentEmail, studentPassword, Role.STUDENT, studentFaculty);

        LOGGER.info("Creating SUPER_ADMIN bootstrap account");
        upsertAccount(superAdminStudentCode, superAdminFullName, superAdminEmail, superAdminPassword, Role.SUPER_ADMIN, superAdminFaculty);
        
        LOGGER.info("=== Test account bootstrap completed ===");
    }

    private void validateSeedConfiguration() {
        if (java.util.stream.Stream.of(
                adminStudentCode, adminFullName, adminEmail, adminFacultyCode,
                studentStudentCode, studentFullName, studentEmail, studentFacultyCode,
                superAdminStudentCode, superAdminFullName, superAdminEmail, superAdminFacultyCode)
                .anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalStateException("Seed accounts are enabled but identity fields are incomplete");
        }
        if (java.util.stream.Stream.of(adminPassword, studentPassword, superAdminPassword)
                .anyMatch(value -> value == null || value.length() < 12)) {
            throw new IllegalStateException("Seed account passwords must contain at least 12 characters");
        }
    }

    @SuppressWarnings("null")
    private Faculty resolveOrCreateFaculty(String code, String name) {
        return facultyRepository.findByCode(code)
                .orElseGet(() -> {
                    LOGGER.info("Faculty {} not found, creating bootstrap faculty", code);
                Faculty faculty = Faculty.builder()
                    .code(code)
                    .name(name)
                    .build();
                return facultyRepository.save(faculty);
                });
    }

    private void upsertAccount(String studentCode,
                               String fullName,
                               String email,
                               String plainPassword,
                               Role role,
                               Faculty faculty) {
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByStudentCode(studentCode))
                .orElseGet(() -> User.builder().email(email).studentCode(studentCode).build());

        LOGGER.debug("Processing bootstrap account - role: {}, faculty: {}", role, faculty.getCode());

        user.setStudentCode(studentCode);
        user.setFullName(fullName);
        user.setRole(role);
        user.setFaculty(faculty);
        user.setVerified(true);

        LOGGER.debug("Resetting password for bootstrap account with role {}", role);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));

        userRepository.save(user);
        LOGGER.info("Bootstrap account upserted successfully - role: {}, verified: true", role);
    }
}
