package com.example.ctu.repository;

import com.example.ctu.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByFaculty_Code(String facultyCode);
}
