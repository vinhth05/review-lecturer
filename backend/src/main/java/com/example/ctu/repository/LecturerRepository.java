package com.example.ctu.repository;

import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.enums.LecturerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {
    List<Lecturer> findByFaculty_CodeAndStatus(String facultyCode, LecturerStatus status);
    List<Lecturer> findBySubject_CodeAndStatus(String subjectCode, LecturerStatus status);
    List<Lecturer> findByStatus(LecturerStatus status);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByFullNameIgnoreCase(String fullName);

    @Query("select l from Lecturer l join fetch l.faculty left join fetch l.subject where l.id = :id")
    Optional<Lecturer> findDetailById(UUID id);
}
