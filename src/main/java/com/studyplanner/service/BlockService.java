package com.studyplanner.service;

import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BlockService {

    private final StudyBlockRepository repository;

    public BlockService(StudyBlockRepository repository) {
        this.repository = repository;
    }

    public StudyBlock markDone(Long id) {
        StudyBlock block = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study block not found."));

        block.setStatus("DONE");
        repository.save(block);
        return block;
    }

    public StudyBlock postpone(Long id, LocalDate newDate, LocalDate today) {
        StudyBlock block = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study block not found."));

        if ("DONE".equals(block.getStatus())) {
            throw new IllegalArgumentException("Cannot postpone a done block.");
        }

        if (newDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot postpone to a past date.");
        }

        if (block.getExamDate() != null && newDate.isAfter(block.getExamDate())) {
            throw new IllegalArgumentException("Cannot postpone past the exam date.");
        }

        block.setDate(newDate);
        block.setPostponedTo(newDate);
        repository.save(block);
        return block;
    }
}