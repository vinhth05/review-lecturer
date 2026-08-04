package com.example.ctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.LecturerRepository;

@ExtendWith(MockitoExtension.class)
class CtuLecturerImportServiceTest {

    @Mock
    private LecturerRepository lecturerRepository;

    @Mock
    private FacultyRepository facultyRepository;

    private CtuLecturerImportService importService;

    @BeforeEach
    void setUp() {
        importService = new CtuLecturerImportService(lecturerRepository, facultyRepository);
    }

    @Test
    void generateLecturerCode_ASCII_returnsCorrectCode() {
        String name = "Nguyen Van A";
        // Base name: NGUYEN VAN A -> NGUYENVANA -> NGUYENVA (8 chars) -> CTUNGUYENVA
        String expected = "CTUNGUYENVA";
        
        when(lecturerRepository.existsByLecturerCode(expected)).thenReturn(false);

        String code = importService.generateLecturerCode(name);
        assertThat(code).isEqualTo(expected);
    }

    @Test
    void generateLecturerCode_WithDong_convertsToD() {
        String name = "Đỗ Giang";
        // Đỗ Giang -> replace Đ/đ -> D/d, normalize diacritics -> DO GIANG -> DOGIANG -> CTUDOGIANG
        String expected = "CTUDOGIANG";

        when(lecturerRepository.existsByLecturerCode(expected)).thenReturn(false);

        String code = importService.generateLecturerCode(name);
        assertThat(code).isEqualTo(expected);
    }

    @Test
    void generateLecturerCode_Duplicate_appendsSuffix() {
        String name = "Nguyen Van A";
        String expectedFirst = "CTUNGUYENVA";
        String expectedSecond = "CTUNGUYENVA01";

        when(lecturerRepository.existsByLecturerCode(expectedFirst)).thenReturn(true);
        when(lecturerRepository.existsByLecturerCode(expectedSecond)).thenReturn(false);

        String code = importService.generateLecturerCode(name);
        assertThat(code).isEqualTo(expectedSecond);
    }
}
