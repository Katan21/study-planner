package com.studyplanner;

import com.studyplanner.dto.StudyBlockDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end smoke test (Sprint 3, Lab 11 — critical user journey through the
 * deployed HTTP interface: create exam -> plan -> mark done -> plan shows done).
 * Owner: Bayastan.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExamPlanSmokeE2ETest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void createExam_plan_markDone_endToEnd() {
        LocalDate today = LocalDate.now();
        String subject = "E2E Physics " + System.nanoTime();

        Map<String, Object> exam = new HashMap<>();
        exam.put("subject", subject);
        exam.put("date", today.plusDays(2).toString());
        exam.put("time", "09:00");

        ResponseEntity<String> created = rest.postForEntity("/api/exams", exam, String.class);
        assertTrue(created.getStatusCode().is2xxSuccessful(), "exam creation should succeed");

        ResponseEntity<StudyBlockDto[]> plan =
                rest.getForEntity("/api/plan?startDate=" + today, StudyBlockDto[].class);
        assertTrue(plan.getStatusCode().is2xxSuccessful(), "plan endpoint should respond");
        StudyBlockDto mine = findBlock(plan.getBody(), subject);
        assertNotNull(mine, "plan should contain a block for the new exam");

        ResponseEntity<String> done =
                rest.postForEntity("/api/blocks/" + mine.getId() + "/done", null, String.class);
        assertTrue(done.getStatusCode().is2xxSuccessful(), "marking done through the API should succeed");

        StudyBlockDto afterDone = findBlock(
                rest.getForEntity("/api/plan?startDate=" + today, StudyBlockDto[].class).getBody(), subject);
        assertEquals("DONE", afterDone.getStatus(), "plan must show the block as DONE");

        // TODO(Bayastan): add postpone happy path through the API:
        //   POST /api/blocks/{id}/postpone?newDate=<date before exam>,
        //   then assert the plan row shows the new date and postponedTo.
        // TODO(Bayastan): add postpone past the exam date -> expect 4xx
        //   (GlobalExceptionHandler maps validation errors to 400),
        //   and assert the plan row keeps its original date.
    }

    private StudyBlockDto findBlock(StudyBlockDto[] blocks, String subject) {
        if (blocks == null) {
            return null;
        }
        for (StudyBlockDto b : blocks) {
            if (subject.equals(b.getSubject())) {
                return b;
            }
        }
        return null;
    }
}
