package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration-level test (Sprint 3, Lab 11 — risk: plan correctness with REAL
 * persistence). Unit tests mock the repository; this one uses the real H2 +
 * JPA stack so upsert/status behaviour is verified end to end inside the app.
 * Owner: Ali.
 */
@SpringBootTest
@Transactional
class PlanPersistenceIntegrationTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private PlanService planService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private StudyBlockRepository blockRepository;

    @Test
    void doneStatusSurvivesPlanRegeneration() {
        LocalDate today = LocalDate.now();
        String subject = "Integration Math " + System.nanoTime();

        Exam exam = new Exam();
        exam.setSubject(subject);
        exam.setDate(today.plusDays(2));
        exam.setTime(LocalTime.of(10, 0));
        examService.add(exam);

        List<StudyBlockDto> plan = planService.generatePlan(today);
        StudyBlockDto mine = plan.stream()
                .filter(b -> subject.equals(b.getSubject()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("block for our exam missing from plan"));
        assertNotNull(mine.getId(), "generated plan rows must have an id");

        blockService.markDone(mine.getId());

        // Re-running the generator must NOT reset the done status (upsert).
        planService.generatePlan(today);

        StudyBlock stored = blockRepository.findById(mine.getId())
                .orElseThrow(() -> new AssertionError("block disappeared after regeneration"));
        assertEquals("DONE", stored.getStatus(), "done status must survive plan regeneration");

        // TODO(Ali): add a postpone-then-regenerate case:
        //   postpone to a date <= examDate, re-run generatePlan,
        //   assert the new date and postponedTo are persisted (not reset).
        // TODO(Ali): add the past-exam-date case against the real repository:
        //   postpone to examDate.plusDays(1) must throw IllegalArgumentException
        //   and the stored block must keep its original date.
    }
}
