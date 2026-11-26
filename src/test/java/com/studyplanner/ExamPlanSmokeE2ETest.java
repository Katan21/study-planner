package com.studyplanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyplanner.dto.StudyBlockDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end smoke test (Sprint 3, Lab 11 — critical user journey through the
 * HTTP interface: create exam -> plan -> mark done -> plan shows done).
 * Uses MockMvc (spring-test) so no extra web-test dependency is needed.
 * Owner: Bayastan.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExamPlanSmokeE2ETest {

    @Autowired
    private MockMvc mockMvc;

    // Built manually: Boot 4 does not expose an ObjectMapper bean in the mock
    // servlet test context by default; JSR-310 module needed for LocalDate fields.
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createExam_plan_markDone_endToEnd() throws Exception {
        LocalDate today = LocalDate.now();
        String subject = "E2E Physics " + System.nanoTime();

        Map<String, Object> exam = new HashMap<>();
        exam.put("subject", subject);
        exam.put("date", today.plusDays(2).toString());
        exam.put("time", "09:00");

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exam)))
                .andExpect(status().is2xxSuccessful());

        MvcResult planResult = mockMvc.perform(get("/api/plan").param("startDate", today.toString()))
                .andExpect(status().isOk())
                .andReturn();
        StudyBlockDto[] plan = objectMapper.readValue(
                planResult.getResponse().getContentAsString(), StudyBlockDto[].class);
        StudyBlockDto mine = findBlock(plan, subject);
        assertNotNull(mine, "plan should contain a block for the new exam");
        assertNotNull(mine.getId(), "plan row must have an id");

        mockMvc.perform(post("/api/blocks/" + mine.getId() + "/done"))
                .andExpect(status().is2xxSuccessful());

        MvcResult afterDone = mockMvc.perform(get("/api/plan").param("startDate", today.toString()))
                .andExpect(status().isOk())
                .andReturn();
        StudyBlockDto doneBlock = findBlock(
                objectMapper.readValue(afterDone.getResponse().getContentAsString(), StudyBlockDto[].class),
                subject);
        assertEquals("DONE", doneBlock.getStatus(), "plan must show the block as DONE");

        // TODO(Bayastan): add postpone happy path through the API:
        //   POST /api/blocks/{id}/postpone?newDate=<date before exam>,
        //   then assert the plan row shows the new date and postponedTo.
        // TODO(Bayastan): add postpone past the exam date -> expect 400
        //   (GlobalExceptionHandler maps validation errors to 400),
        //   and assert the plan row keeps its original date.
    }

    private StudyBlockDto findBlock(StudyBlockDto[] blocks, String subject) {
        for (StudyBlockDto b : blocks) {
            if (subject.equals(b.getSubject())) {
                return b;
            }
        }
        return null;
    }
}
