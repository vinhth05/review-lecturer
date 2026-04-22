package com.example.ctu.repository;

import com.example.ctu.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    Optional<Faculty> findByCode(String code);
    java.util.List<Faculty> findAllByOrderByNameAsc();
}
