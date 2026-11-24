package com.studyplanner.repository;

import com.studyplanner.model.StudyBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StudyBlockRepository extends JpaRepository<StudyBlock, Long> {
    Optional<StudyBlock> findBySubjectAndExamDateAndTask(String subject, LocalDate examDate, String task);
}