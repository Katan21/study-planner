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

    @GetMapping("/api/plan")
    public List<StudyBlockDto> getPlan(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        return planService.generatePlan(startDate);
    }
}