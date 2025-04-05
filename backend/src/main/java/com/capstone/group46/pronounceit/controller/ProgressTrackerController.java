package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.ProgressTrackerEntity;
import com.capstone.group46.pronounceit.service.ProgressTrackerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress-trackers")
public class ProgressTrackerController {
    private final ProgressTrackerService progressTrackerService;

    public ProgressTrackerController(ProgressTrackerService progressTrackerService) {
        this.progressTrackerService = progressTrackerService;
    }

    @GetMapping("/{progressId}")
    public ResponseEntity<ProgressTrackerEntity> getProgressTrackerById(@PathVariable Long progressId) {
        return progressTrackerService.getProgressTrackerById(progressId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProgressTrackerEntity>> getAllProgressTrackers() {
        List<ProgressTrackerEntity> progressTrackers = progressTrackerService.getAllProgressTrackers();
        return ResponseEntity.ok(progressTrackers);
    }

    @PostMapping
    public ProgressTrackerEntity createProgressTracker(@RequestBody ProgressTrackerEntity progressTracker) {
        return progressTrackerService.createProgressTracker(progressTracker);
    }

    @PutMapping("/{progressId}")
    public ResponseEntity<ProgressTrackerEntity> updateProgressTracker(@PathVariable Long progressId, @RequestBody ProgressTrackerEntity updatedProgressTracker) {
        return progressTrackerService.updateProgressTracker(progressId, updatedProgressTracker)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{progressId}")
    public ResponseEntity<Void> deleteProgressTracker(@PathVariable Long progressId) {
        progressTrackerService.deleteProgressTracker(progressId);
        return ResponseEntity.noContent().build();
    }
}