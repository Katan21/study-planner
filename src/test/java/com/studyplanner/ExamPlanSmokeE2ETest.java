package com.studyplanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end smoke test (Sprint 3, Lab 11 — critical user journey through the
 * HTTP interface: create exam -> plan -> mark done -> plan shows done).
 * Uses MockMvc + JsonNode so no extra test dependencies are needed.
 * Owner: Bayastan.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExamPlanSmokeE2ETest {

    @Autowired
    private MockMvc mockMvc;

    // Built manually (Boot 4 exposes no ObjectMapper bean in the mock servlet
    // context by default); we only read string/number fields, no date binding.
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        JsonNode mine = findBlock(getPlan(today), subject);
        assertNotNull(mine, "plan should contain a block for the new exam");
        assertTrue(mine.get("id").canConvertToLong(), "plan row must have an id");

        mockMvc.perform(post("/api/blocks/" + mine.get("id").asLong() + "/done"))
                .andExpect(status().is2xxSuccessful());

        JsonNode doneBlock = findBlock(getPlan(today), subject);
        assertEquals("DONE", doneBlock.get("status").asText(), "plan must show the block as DONE");

        // TODO(Bayastan): add postpone happy path through the API:
        //   POST /api/blocks/{id}/postpone?newDate=<date before exam>,
        //   then assert the plan row shows the new date and postponedTo.
        // TODO(Bayastan): add postpone past the exam date -> expect 400
        //   (GlobalExceptionHandler maps validation errors to 400),
        //   and assert the plan row keeps its original date.
    }

    private JsonNode getPlan(LocalDate startDate) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/plan").param("startDate", startDate.toString()))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode findBlock(JsonNode blocks, String subject) {
        for (JsonNode b : blocks) {
            if (subject.equals(b.path("subject").asText())) {
                return b;
            }
        }
        return null;
    }
}
