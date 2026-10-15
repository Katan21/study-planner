package com.studyplanner.service;

import com.studyplanner.model.Exam;
import com.studyplanner.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<Exam> getAll() {
        return examRepository.findAll();
    }

    public Exam add(Exam exam) {
        validateExam(exam);
        
        if (examRepository.existsByDateAndSubject(exam.getDate(), exam.getSubject())) {
            throw new IllegalArgumentException("An exam with this subject already exists on this date.");
        }
        
        return examRepository.save(exam);
    }

    public Exam update(Long id, Exam examDetails) {
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + id));

        validateExam(examDetails);

        // Check if date or subject changed and if it creates a duplicate
        boolean dateOrSubjectChanged = !existingExam.getDate().equals(examDetails.getDate()) 
                || !existingExam.getSubject().equals(examDetails.getSubject());

        if (dateOrSubjectChanged && examRepository.existsByDateAndSubject(examDetails.getDate(), examDetails.getSubject())) {
            throw new IllegalArgumentException("An exam with this subject already exists on this date.");
        }

        existingExam.setSubject(examDetails.getSubject());
        existingExam.setDate(examDetails.getDate());
        existingExam.setTime(examDetails.getTime());

        return examRepository.save(existingExam);
    }

    public void delete(Long id) {
        if (!examRepository.existsById(id)) {
            throw new IllegalArgumentException("Exam not found with id: " + id);
        }
        examRepository.deleteById(id);
    }

    private void validateExam(Exam exam) {
        if (exam.getSubject() == null || exam.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (exam.getDate() == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (exam.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Exam date must be today or in the future.");
        }
    }
}