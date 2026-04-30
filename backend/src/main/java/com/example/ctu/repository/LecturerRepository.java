package com.example.ctu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.enums.LecturerStatus;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    List<Lecturer> findByFaculty_CodeAndStatus(String facultyCode, LecturerStatus status);
    List<Lecturer> findBySubject_CodeAndStatus(String subjectCode, LecturerStatus status);
    List<Lecturer> findByStatus(LecturerStatus status);

    Page<Lecturer> findByFaculty_CodeAndStatus(String facultyCode, LecturerStatus status, Pageable pageable);
    Page<Lecturer> findBySubject_CodeAndStatus(String subjectCode, LecturerStatus status, Pageable pageable);
    Page<Lecturer> findByStatus(LecturerStatus status, Pageable pageable);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByFullNameIgnoreCase(String fullName);

    @Query("select l from Lecturer l join fetch l.faculty left join fetch l.subject where l.id = :id")
    Optional<Lecturer> findDetailById(Long id);
}
