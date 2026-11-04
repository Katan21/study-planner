package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import com.studyplanner.repository.ExamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    void testEmptyPlan_NoExams() {
        when(examRepository.findAll()).thenReturn(Collections.emptyList());

        List<StudyBlockDto> plan = planService.generatePlan(LocalDate.of(2026, 11, 1));
        assertTrue(plan.isEmpty());
    }

    @Test
    void testSingleExam_GeneratesBlocksAndFinalRevision() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        Exam exam = new Exam("Math", LocalDate.of(2026, 11, 3), LocalTime.of(10, 0));
        when(examRepository.findAll()).thenReturn(List.of(exam));

        List<StudyBlockDto> plan = planService.generatePlan(today);

        assertEquals(3, plan.size());
        assertEquals("study", plan.get(0).getActivity());
        assertEquals("study", plan.get(1).getActivity());
        assertEquals("final revision", plan.get(2).getActivity());
        assertEquals("Math", plan.get(2).getSubject());
    }

    @Test
    void testBoundary_TwoExamsOnAdjacentDates() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        Exam exam1 = new Exam("Math", LocalDate.of(2026, 11, 2), LocalTime.of(10, 0));
        Exam exam2 = new Exam("Java", LocalDate.of(2026, 11, 3), LocalTime.of(12, 0));
        when(examRepository.findAll()).thenReturn(List.of(exam1, exam2));

        List<StudyBlockDto> plan = planService.generatePlan(today);

        assertFalse(plan.isEmpty());
        // Проверяем, что в день первого экзамена ставится final revision для первого,
        // а ближайший экзамен имеет приоритет
        assertEquals("Math", plan.get(0).getSubject());
    }
}