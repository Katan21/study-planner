package com.studyplanner.service;

import com.studyplanner.model.StudyBlock;
import com.studyplanner.repository.StudyBlockRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockServiceTest {

    private final StudyBlockRepository repo = mock(StudyBlockRepository.class);
    private final BlockService service = new BlockService(repo);
    private final LocalDate today = LocalDate.of(2026, 11, 17);

    private StudyBlock block(LocalDate examDate, String status) {
        StudyBlock b = new StudyBlock();
        b.setSubject("Math");
        b.setDate(today);
        b.setTask("Study — Math");
        b.setStatus(status);
        b.setExamDate(examDate);
        return b;
    }

    @Test
    void markDone_setsStatusAndPersists() {
        StudyBlock b = block(today.plusDays(5), "TODO");
        when(repo.findById(1L)).thenReturn(Optional.of(b));
        StudyBlock result = service.markDone(1L);
        assertEquals("DONE", result.getStatus());
        verify(repo).save(b);
    }

    @Test
    void postpone_movesBlockToNewDay() {
        StudyBlock b = block(today.plusDays(5), "TODO");
        when(repo.findById(2L)).thenReturn(Optional.of(b));
        LocalDate newDay = today.plusDays(3);
        StudyBlock result = service.postpone(2L, newDay, today);
        assertEquals(newDay, result.getDate());
        verify(repo).save(b);
    }

    @Test
    void postpone_pastExamDate_rejected() {
        StudyBlock b = block(today.plusDays(1), "TODO");
        when(repo.findById(3L)).thenReturn(Optional.of(b));
        LocalDate tooLate = today.plusDays(4);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.postpone(3L, tooLate, today));
        assertEquals("Cannot postpone past the exam date.", ex.getMessage());
    }

    @Test
    void postpone_toPastDay_rejected() {
        StudyBlock b = block(today.plusDays(5), "TODO");
        when(repo.findById(4L)).thenReturn(Optional.of(b));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.postpone(4L, today.minusDays(1), today));
        assertEquals("Cannot postpone to a past date.", ex.getMessage());
    }

    @Test
    void postpone_doneBlock_rejected() {
        StudyBlock b = block(today.plusDays(5), "DONE");
        when(repo.findById(5L)).thenReturn(Optional.of(b));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.postpone(5L, today.plusDays(2), today));
        assertEquals("Cannot postpone a done block.", ex.getMessage());
    }
}
