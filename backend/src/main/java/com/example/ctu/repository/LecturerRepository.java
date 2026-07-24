package com.example.ctu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.enums.LecturerStatus;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    @EntityGraph(attributePaths = {"faculty", "subject"})
    List<Lecturer> findByFaculty_CodeAndStatus(String facultyCode, LecturerStatus status);

    @EntityGraph(attributePaths = {"faculty", "subject"})
    List<Lecturer> findBySubject_CodeAndStatus(String subjectCode, LecturerStatus status);

    @EntityGraph(attributePaths = {"faculty", "subject"})
    List<Lecturer> findByStatus(LecturerStatus status);
    
    long countByFaculty_Id(Long facultyId);
    long countBySubject_Id(Long subjectId);

    @EntityGraph(attributePaths = {"faculty", "subject"})
    Page<Lecturer> findByFaculty_CodeAndStatus(String facultyCode, LecturerStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"faculty", "subject"})
    Page<Lecturer> findBySubject_CodeAndStatus(String subjectCode, LecturerStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"faculty", "subject"})
    Page<Lecturer> findByStatus(LecturerStatus status, Pageable pageable);
    
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByFullNameIgnoreCase(String fullName);

    @Query("select l from Lecturer l join fetch l.faculty left join fetch l.subject where l.id = :id")
    Optional<Lecturer> findDetailById(Long id);

    @Query("select l from Lecturer l " +
           "where l.status = :status " +
           "and (:facultyCode is null or :facultyCode = '' or l.faculty.code = :facultyCode) " +
           "and (:subjectCode is null or :subjectCode = '' or l.subject.code = :subjectCode) " +
           "and (:search is null or :search = '' or lower(l.fullName) like lower(concat('%', :search, '%')))")
    @EntityGraph(attributePaths = {"faculty", "subject"})
    Page<Lecturer> searchLecturers(String facultyCode, String subjectCode, String search, LecturerStatus status, Pageable pageable);

    @Query("select l from Lecturer l " +
           "where l.status = :status " +
           "and (:facultyCode is null or :facultyCode = '' or l.faculty.code = :facultyCode) " +
           "and (:subjectCode is null or :subjectCode = '' or l.subject.code = :subjectCode) " +
           "and (:search is null or :search = '' or lower(l.fullName) like lower(concat('%', :search, '%')))")
    @EntityGraph(attributePaths = {"faculty", "subject"})
    List<Lecturer> searchLecturers(String facultyCode, String subjectCode, String search, LecturerStatus status);

    @Query("select l from Lecturer l " +
           "where (:keyword is null or :keyword = '' or lower(l.fullName) like lower(concat('%', :keyword, '%')) or lower(l.lecturerCode) like lower(concat('%', :keyword, '%')))")
    @EntityGraph(attributePaths = {"faculty", "subject"})
    Page<Lecturer> searchAllLecturers(String keyword, Pageable pageable);

    @Query("select l.faculty.id as facultyId, count(l.id) as lecturerCount from Lecturer l group by l.faculty.id")
    List<FacultyLecturerCountProjection> findLecturerCountByFaculty();
}
