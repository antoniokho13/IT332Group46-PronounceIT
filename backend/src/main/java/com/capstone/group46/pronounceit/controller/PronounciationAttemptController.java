package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;
import com.capstone.group46.pronounceit.service.PronounciationAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pronounciation-attempts")
public class PronounciationAttemptController {
    private final PronounciationAttemptService pronounciationAttemptService;

    public PronounciationAttemptController(PronounciationAttemptService pronounciationAttemptService) {
        this.pronounciationAttemptService = pronounciationAttemptService;
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<PronounciationAttemptEntity> getPronounciationAttemptById(@PathVariable Long attemptId) {
        return pronounciationAttemptService.getPronounciationAttemptById(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PronounciationAttemptEntity>> getAllPronounciationAttempts() {
        List<PronounciationAttemptEntity> attempts = pronounciationAttemptService.getAllPronounciationAttempts();
        return ResponseEntity.ok(attempts);
    }

    @PostMapping
    public PronounciationAttemptEntity createPronounciationAttempt(@RequestBody PronounciationAttemptEntity pronounciationAttempt) {
        return pronounciationAttemptService.createPronounciationAttempt(pronounciationAttempt);
    }

    @PutMapping("/{attemptId}")
    public ResponseEntity<PronounciationAttemptEntity> updatePronounciationAttempt(@PathVariable Long attemptId, @RequestBody PronounciationAttemptEntity updatedPronounciationAttempt) {
        return pronounciationAttemptService.updatePronounciationAttempt(attemptId, updatedPronounciationAttempt)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{attemptId}")
    public ResponseEntity<Void> deletePronounciationAttempt(@PathVariable Long attemptId) {
        pronounciationAttemptService.deletePronounciationAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }
}