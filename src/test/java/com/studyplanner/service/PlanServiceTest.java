package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private ExamService examService;

    @InjectMocks
    private PlanService planService;

    @Test
    void generatePlan_doneStaysDone() {
        ExamService examService = mock(ExamService.class);
        StudyBlockRepository blockRepo = mock(StudyBlockRepository.class);
        PlanService planService = new PlanService(examService, blockRepo);

        LocalDate today = LocalDate.of(2026, 11, 24);
        Exam exam = new Exam();
        exam.setSubject("Math");
        exam.setDate(today);
        exam.setTime(LocalTime.of(10, 0));

        when(examService.getAll()).thenReturn(List.of(exam));

        StudyBlock completedBlock = new StudyBlock();
        completedBlock.setId(42L);
        completedBlock.setDate(today);
        completedBlock.setSubject("Math");
        completedBlock.setTask("final revision - " + today);
        completedBlock.setStatus("DONE");
        completedBlock.setExamDate(today);

        when(blockRepo.findBySubjectAndExamDateAndTask("Math", today, "final revision - " + today))
                .thenReturn(Optional.of(completedBlock));

        List<StudyBlockDto> result = planService.generatePlan(today);

        assertEquals(1, result.size());
        assertEquals(42L, result.get(0).getId());
        assertEquals("DONE", result.get(0).getStatus());
        verify(blockRepo, never()).save(any());
    }
    
    void testEmptyPlan_NoExams() {
        when(examService.getAll()).thenReturn(Collections.emptyList());

        List<StudyBlockDto> plan = planService.generatePlan(LocalDate.of(2026, 11, 1));
        assertTrue(plan.isEmpty());
    }

    @Test
    void testSingleExam_GeneratesBlocksAndFinalRevision() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        Exam exam = new Exam("Math", LocalDate.of(2026, 11, 3), LocalTime.of(10, 0));
        when(examService.getAll()).thenReturn(List.of(exam));

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
        when(examService.getAll()).thenReturn(List.of(exam1, exam2));

        List<StudyBlockDto> plan = planService.generatePlan(today);

        assertFalse(plan.isEmpty());
        assertEquals("Math", plan.get(0).getSubject());
    }
}