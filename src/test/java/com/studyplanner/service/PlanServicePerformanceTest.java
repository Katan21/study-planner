package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Quality scenario (Lab 09): generating a study plan for a realistic heavy
 * load — 50 exams — must complete within 300 ms per trigger (measured as the
 * worst of 5 runs, in-process, mocked persistence so we measure the rule,
 * not the database).
 */
class PlanServicePerformanceTest {

    private static final int EXAM_COUNT = 50;
    private static final long BUDGET_MILLIS = 300;
    private static final int RUNS = 5;

    @Test
    void generatePlan_worstOfFiveRunsUnder300msFor50Exams() {
        ExamService examService = mock(ExamService.class);
        StudyBlockRepository blockRepo = mock(StudyBlockRepository.class);
        PlanService planService = new PlanService(examService, blockRepo);

        LocalDate today = LocalDate.of(2026, 12, 2);
        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < EXAM_COUNT; i++) {
            Exam e = new Exam();
            e.setSubject("Subject " + i);
            e.setDate(today.plusDays(1 + (i % 60)));
            e.setTime(LocalTime.of(9 + (i % 8), 0));
            exams.add(e);
        }
        when(examService.getAll()).thenReturn(exams);
        when(blockRepo.findById(anyLong())).thenReturn(Optional.empty());
        when(blockRepo.save(any(StudyBlock.class))).thenAnswer(inv -> inv.getArgument(0));

        long worst = 0;
        for (int run = 0; run < RUNS; run++) {
            long start = System.nanoTime();
            List<StudyBlockDto> plan = planService.generatePlan(today);
            long millis = (System.nanoTime() - start) / 1_000_000;
            worst = Math.max(worst, millis);
            assertFalse(plan.isEmpty(), "plan must not be empty for 50 exams");
        }
        assertTrue(worst < BUDGET_MILLIS,
                "Quality scenario failed: worst run took " + worst + " ms (budget " + BUDGET_MILLIS + " ms)");
    }
}
