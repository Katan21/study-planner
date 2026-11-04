package com.studyplanner.service;

import com.studyplanner.model.Exam;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountdownServiceTest {

    private final ExamService examService = mock(ExamService.class);
    private final CountdownService service = new CountdownService(examService);
    private final LocalDate today = LocalDate.of(2026, 11, 4);

    @Test
    void noExams_returnsEmpty() {
        when(examService.getAll()).thenReturn(Collections.emptyList());
        assertEquals(Optional.empty(), service.daysUntilNearestExam(today));
    }

    @Test
    void examTomorrow_returnsOne() {
        Exam e = new Exam();
        e.setSubject("Math");
        e.setDate(today.plusDays(1));
        when(examService.getAll()).thenReturn(List.of(e));
        assertEquals(Optional.of(1L), service.daysUntilNearestExam(today));
    }

    @Test
    void pastExamIgnored_noNegativeCountdown() {
        Exam past = new Exam();
        past.setSubject("Old");
        past.setDate(today.minusDays(3));
        when(examService.getAll()).thenReturn(List.of(past));
        assertEquals(Optional.empty(), service.daysUntilNearestExam(today));
    }

    @Test
    void nearestOfSeveralExams() {
        Exam far = new Exam();
        far.setSubject("A");
        far.setDate(today.plusDays(10));
        Exam near = new Exam();
        near.setSubject("B");
        near.setDate(today.plusDays(2));
        when(examService.getAll()).thenReturn(List.of(far, near));
        assertEquals(Optional.of(2L), service.daysUntilNearestExam(today));
    }
}
