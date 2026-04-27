package com.example.ctu.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
            @Value("${app.seed.accounts.admin.student-code:AD0001}") String adminStudentCode,
            @Value("${app.seed.accounts.admin.full-name:Quan tri vien CTU}") String adminFullName,
            @Value("${app.seed.accounts.admin.email:admin@ctu.edu.vn}") String adminEmail,
            @Value("${app.seed.accounts.admin.password:Admin@123}") String adminPassword,
            @Value("${app.seed.accounts.admin.faculty-code:ICT}") String adminFacultyCode,
            @Value("${app.seed.accounts.student.student-code:SV0001}") String studentStudentCode,
            @Value("${app.seed.accounts.student.full-name:Sinh vien Demo}") String studentFullName,
            @Value("${app.seed.accounts.student.email:student01@student.ctu.edu.vn}") String studentEmail,
            @Value("${app.seed.accounts.student.password:Student@123}") String studentPassword,
            @Value("${app.seed.accounts.student.faculty-code:ECO}") String studentFacultyCode,
            @Value("${app.seed.accounts.super-admin.student-code:SA0001}") String superAdminStudentCode,
            @Value("${app.seed.accounts.super-admin.full-name:Super Admin CTU}") String superAdminFullName,
            @Value("${app.seed.accounts.super-admin.email:superadmin@ctu.edu.vn}") String superAdminEmail,
            @Value("${app.seed.accounts.super-admin.password:Super@123}") String superAdminPassword,
            @Value("${app.seed.accounts.super-admin.faculty-code:ICT}") String superAdminFacultyCode
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
        LOGGER.info("=== Starting test account bootstrap ===");
        LOGGER.info("Admin faculty code: {}", adminFacultyCode);
        LOGGER.info("Student faculty code: {}", studentFacultyCode);
        LOGGER.info("SuperAdmin faculty code: {}", superAdminFacultyCode);
        
        Optional<Faculty> adminFaculty = resolveFaculty(adminFacultyCode);
        Optional<Faculty> studentFaculty = resolveFaculty(studentFacultyCode);
        Optional<Faculty> superAdminFaculty = resolveFaculty(superAdminFacultyCode);

        LOGGER.info("Admin faculty resolved: {}", adminFaculty.map(Faculty::getCode).orElse("NONE"));
        LOGGER.info("Student faculty resolved: {}", studentFaculty.map(Faculty::getCode).orElse("NONE"));
        LOGGER.info("SuperAdmin faculty resolved: {}", superAdminFaculty.map(Faculty::getCode).orElse("NONE"));

        if (adminFaculty.isEmpty() || studentFaculty.isEmpty() || superAdminFaculty.isEmpty()) {
            LOGGER.warn("Skip test account bootstrap because faculties are not available.");
            return;
        }

        LOGGER.info("Creating ADMIN account: {}", adminEmail);
        upsertAccount(adminStudentCode, adminFullName, adminEmail, adminPassword, Role.ADMIN, adminFaculty.get());
        
        LOGGER.info("Creating STUDENT account: {}", studentEmail);
        upsertAccount(studentStudentCode, studentFullName, studentEmail, studentPassword, Role.STUDENT, studentFaculty.get());
        
        LOGGER.info("Creating SUPER_ADMIN account: {}", superAdminEmail);
        upsertAccount(superAdminStudentCode, superAdminFullName, superAdminEmail, superAdminPassword, Role.SUPER_ADMIN, superAdminFaculty.get());
        
        LOGGER.info("=== Test account bootstrap completed ===");
    }

    private Optional<Faculty> resolveFaculty(String preferredCode) {
        Optional<Faculty> byPreferredCode = facultyRepository.findByCode(preferredCode);
        if (byPreferredCode.isPresent()) {
            return byPreferredCode;
        }

        Optional<Faculty> ict = facultyRepository.findByCode("ICT");
        if (ict.isPresent()) {
            return ict;
        }

        Optional<Faculty> eco = facultyRepository.findByCode("ECO");
        if (eco.isPresent()) {
            return eco;
        }

        return facultyRepository.findAllByOrderByNameAsc().stream().findFirst();
    }

    private void upsertAccount(String studentCode,
                               String fullName,
                               String email,
                               String plainPassword,
                               Role role,
                               Faculty faculty) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder().email(email).build());

        LOGGER.debug("Processing account - email: {}, role: {}, faculty: {}", email, role, faculty.getCode());

        user.setStudentCode(studentCode);
        user.setFullName(fullName);
        user.setRole(role);
        user.setFaculty(faculty);
        user.setVerified(true);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            LOGGER.debug("Encoding password for: {}", email);
            user.setPasswordHash(passwordEncoder.encode(plainPassword));
        }

        userRepository.save(user);
        LOGGER.info("Account upserted successfully - email: {}, role: {}, verified: true", email, role);
    }
}