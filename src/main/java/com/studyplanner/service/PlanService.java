package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanService {

    private final ExamService examService;

    public PlanService(ExamService examService) {
        this.examService = examService;
    }

    public List<StudyBlockDto> generatePlan(LocalDate startDate) {
        List<Exam> exams = examService.getAll();
        if (exams.isEmpty()) {
            return Collections.emptyList();
        }

        exams.sort(Comparator.comparing(Exam::getDate));
        List<StudyBlockDto> plan = new ArrayList<>();
        Exam nearestExam = exams.get(0);

        LocalDate current = startDate;
        while (!current.isAfter(nearestExam.getDate())) {
            if (current.equals(nearestExam.getDate())) {
                plan.add(new StudyBlockDto(current, nearestExam.getSubject(), "final revision"));
            } else {
                plan.add(new StudyBlockDto(current, nearestExam.getSubject(), "study"));
            }
            current = current.plusDays(1);
        }

        return plan;
    }
}