package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanService {

    private static final String ACTIVITY_STUDY = "study";
    private static final String ACTIVITY_FINAL_REVISION = "final revision";

    private final ExamService examService;

    public PlanService(ExamService examService) {
        this.examService = examService;
    }

    public List<StudyBlockDto> generatePlan(LocalDate startDate) {
        List<Exam> upcomingExams = examService.getAll().stream()
                .filter(exam -> !exam.getDate().isBefore(startDate))
                .sorted(Comparator.comparing(Exam::getDate).thenComparing(Exam::getTime))
                .toList();

        if (upcomingExams.isEmpty()) {
            return Collections.emptyList();
        }

        Exam priorityExam = upcomingExams.get(0);
        List<StudyBlockDto> plan = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(priorityExam.getDate()); date = date.plusDays(1)) {
            String activity = date.equals(priorityExam.getDate()) ? ACTIVITY_FINAL_REVISION : ACTIVITY_STUDY;
            plan.add(new StudyBlockDto(date, priorityExam.getSubject(), activity));
        }

        return plan;
    }
}