package com.example.ctu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.ctu.entity.User;
import com.example.ctu.entity.enums.Role;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentCode(String studentCode);
    boolean existsByEmail(String email);
    boolean existsByStudentCode(String studentCode);
    long countByRole(Role role);
    long countByFaculty_Id(Long facultyId);
}
