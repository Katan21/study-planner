package com.studyplanner.controller;

import com.studyplanner.model.StudyBlock;
import com.studyplanner.service.BlockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{id}/done")
    public StudyBlock markDone(@PathVariable Long id) {
        return blockService.markDone(id);
    }

    @PostMapping("/{id}/postpone")
    public StudyBlock postpone(
            @PathVariable Long id,
            @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
        return blockService.postpone(id, newDate, LocalDate.now());
    }
}
