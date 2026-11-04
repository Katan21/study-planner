package com.studyplanner.service;

import com.studyplanner.model.Exam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CountdownService {

    private final ExamService examService;

    public CountdownService(ExamService examService) {
        this.examService = examService;
    }

    public Optional<Long> daysUntilNearestExam(LocalDate from) {
        List<Exam> exams = examService.getAll();
        return exams.stream()
                .map(Exam::getDate)
                .filter(d -> d != null && !d.isBefore(from))
                .min(LocalDate::compareTo)
                .map(d -> ChronoUnit.DAYS.between(from, d));
    }
}
