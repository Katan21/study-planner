package com.studyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studyplanner.model.Exam;

import java.time.LocalDate;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    boolean existsByDateAndSubject(LocalDate date, String subject);
}
