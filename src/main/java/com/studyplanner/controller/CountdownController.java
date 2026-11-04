package com.studyplanner.controller;

import com.studyplanner.service.CountdownService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/countdown")
@CrossOrigin(origins = "*")
public class CountdownController {

    private final CountdownService countdownService;

    public CountdownController(CountdownService countdownService) {
        this.countdownService = countdownService;
    }

    @GetMapping
    public Map<String, Object> countdown() {
        Optional<Long> days = countdownService.daysUntilNearestExam(LocalDate.now());
        return Map.of(
                "daysLeft", days.orElse(-1L),
                "warning", days.map(d -> d <= 2).orElse(false)
        );
    }
