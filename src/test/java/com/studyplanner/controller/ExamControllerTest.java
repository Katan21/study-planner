package com.studyplanner.controller;

import com.studyplanner.model.Exam;
import com.studyplanner.service.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ExamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExamService examService;

    @InjectMocks
    private ExamController examController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(examController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetAllExamsReturns200() throws Exception {
        given(examService.getAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/exams"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPostValidExamReturns201() throws Exception {
        Exam exam = new Exam("Math", LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        given(examService.add(any(Exam.class))).willReturn(exam);

        String examJson = "{\"subject\":\"Math\",\"date\":\"" + LocalDate.now().plusDays(1) + "\",\"time\":\"10:00:00\"}";

        mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(examJson))
                .andExpect(status().isCreated());
    }

    @Test
    public void testPostPastDateReturns400() throws Exception {
        String examJson = "{\"subject\":\"Math\",\"date\":\"2025-01-01\",\"time\":\"10:00:00\"}";
        given(examService.add(any(Exam.class))).willThrow(new IllegalArgumentException("Exam date must be today or in the future."));

        mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(examJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostDuplicateReturns400() throws Exception {
        String examJson = "{\"subject\":\"Math\",\"date\":\"" + LocalDate.now().plusDays(1) + "\",\"time\":\"10:00:00\"}";
        given(examService.add(any(Exam.class))).willThrow(new IllegalArgumentException("An exam with this subject already exists on this date."));

        mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(examJson))
                .andExpect(status().isBadRequest());
    }
}