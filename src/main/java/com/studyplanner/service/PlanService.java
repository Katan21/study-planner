
package com.studyplanner.service;

import com.studyplanner.dto.StudyBlockDto;
import com.studyplanner.model.Exam;
import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PlanService {

    private static final String ACTIVITY_STUDY = "study";
    private static final String ACTIVITY_FINAL_REVISION = "final revision";

    private final ExamService examService;
    private final StudyBlockRepository studyBlockRepository;

    public PlanService(ExamService examService) {
        this(examService, null);
    }

    @Autowired
    public PlanService(ExamService examService, StudyBlockRepository studyBlockRepository) {
        this.examService = examService;
        this.studyBlockRepository = studyBlockRepository;
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
            String activity = date.equals(priorityExam.getDate())
                    ? ACTIVITY_FINAL_REVISION
                    : ACTIVITY_STUDY;

            String taskKey = activity + " - " + date;

            if (studyBlockRepository == null) {
                plan.add(new StudyBlockDto(
                        null,
                        date,
                        priorityExam.getSubject(),
                        activity,
                        "TODO",
                        priorityExam.getDate(),
                        priorityExam.getTime(),
                        null
                ));
                continue;
            }

            StudyBlock existing = studyBlockRepository
                    .findBySubjectAndExamDateAndTask(
                            priorityExam.getSubject(),
                            priorityExam.getDate(),
                            taskKey
                    )
                    .orElse(null);

            if (existing != null) {
                plan.add(new StudyBlockDto(
                        existing.getId(),
                        existing.getDate(),
                        existing.getSubject(),
                        activity,
                        existing.getStatus(),
                        existing.getExamDate(),
                        priorityExam.getTime(),
                        existing.getPostponedTo()
                ));
            } else {
                StudyBlock created = new StudyBlock();
                created.setDate(date);
                created.setSubject(priorityExam.getSubject());
                created.setTask(taskKey);
                created.setStatus("TODO");
                created.setExamDate(priorityExam.getDate());

                StudyBlock saved = studyBlockRepository.save(created);
                StudyBlock blockToUse = saved != null ? saved : created;

                plan.add(new StudyBlockDto(
                        blockToUse.getId(),
                        blockToUse.getDate(),
                        blockToUse.getSubject(),
                        activity,
                        blockToUse.getStatus(),
                        blockToUse.getExamDate(),
                        priorityExam.getTime(),
                        blockToUse.getPostponedTo()
                ));
            }
        }

        return plan;
    }
}
