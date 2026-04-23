package com.example.ctu.repository;

import com.example.ctu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentCode(String studentCode);
    boolean existsByEmail(String email);
    boolean existsByStudentCode(String studentCode);
}
