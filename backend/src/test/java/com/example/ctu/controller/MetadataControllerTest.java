package com.example.ctu.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Subject;
import com.example.ctu.exception.GlobalExceptionHandler;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class MetadataControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private MetadataController metadataController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metadataController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void faculties_ReturnsWrappedFacultyResponseList() throws Exception {
        Faculty faculty = Faculty.builder().id(1L).name("Công nghệ thông tin").code("ICT").build();
        when(facultyRepository.findAllByOrderByNameAsc()).thenReturn(List.of(faculty));

        mockMvc.perform(get("/metadata/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Công nghệ thông tin"))
                .andExpect(jsonPath("$.data[0].code").value("ICT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void subjects_WithoutFacultyCode_ReturnsAllSubjects() throws Exception {
        Faculty faculty = Faculty.builder().id(1L).name("Công nghệ thông tin").code("ICT").build();
        Subject subject = Subject.builder().id(10L).name("Lập trình Java").code("CT176").faculty(faculty).build();
        when(subjectRepository.findAllByOrderByFaculty_NameAscNameAsc()).thenReturn(List.of(subject));

        mockMvc.perform(get("/metadata/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].name").value("Lập trình Java"))
                .andExpect(jsonPath("$.data[0].code").value("CT176"))
                .andExpect(jsonPath("$.data[0].facultyId").value(1))
                .andExpect(jsonPath("$.data[0].facultyCode").value("ICT"))
                .andExpect(jsonPath("$.data[0].facultyName").value("Công nghệ thông tin"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void subjects_WithFacultyCode_ReturnsFilteredSubjects() throws Exception {
        Faculty faculty = Faculty.builder().id(1L).name("Công nghệ thông tin").code("ICT").build();
        Subject subject = Subject.builder().id(10L).name("Lập trình Java").code("CT176").faculty(faculty).build();
        when(subjectRepository.findByFaculty_CodeOrderByNameAsc("ICT")).thenReturn(List.of(subject));

        mockMvc.perform(get("/metadata/subjects?facultyCode=ICT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].facultyCode").value("ICT"));
    }
}
