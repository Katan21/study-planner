package com.studyplanner.controller;

import com.studyplanner.model.StudyBlock;
import com.studyplanner.service.BlockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PutMapping("/{id}/done")
    public StudyBlock markDone(@PathVariable Long id) {
        return blockService.markDone(id);
    }

    @PutMapping("/{id}/postpone")
    public StudyBlock postpone(
            @PathVariable Long id,
            @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
        return blockService.postpone(id, newDate, LocalDate.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}