package com.studyplanner.controller;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.service.PlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }
        //Ali
    @GetMapping("/api/plan")
    public List<StudyBlockDto> getPlan(
            @RequestParam("startDate") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return planService.generatePlan(startDate);
    }
}
