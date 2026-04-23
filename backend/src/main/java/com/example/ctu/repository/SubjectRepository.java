package com.example.ctu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ctu.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findAllByOrderByFaculty_NameAscNameAsc();

    List<Subject> findByFaculty_CodeOrderByNameAsc(String facultyCode);
}
