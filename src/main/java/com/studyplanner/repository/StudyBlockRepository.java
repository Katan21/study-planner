package com.studyplanner.repository;

import com.studyplanner.model.StudyBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyBlockRepository extends JpaRepository<StudyBlock, Long> {
}
