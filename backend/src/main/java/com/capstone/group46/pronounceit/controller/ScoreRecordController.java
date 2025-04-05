package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.ScoreRecordEntity;
import com.capstone.group46.pronounceit.service.ScoreRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score-records")
public class ScoreRecordController {
    private final ScoreRecordService scoreRecordService;

    public ScoreRecordController(ScoreRecordService scoreRecordService) {
        this.scoreRecordService = scoreRecordService;
    }

    @GetMapping("/{scoreId}")
    public ResponseEntity<ScoreRecordEntity> getScoreRecordById(@PathVariable Long scoreId) {
        return scoreRecordService.getScoreRecordById(scoreId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ScoreRecordEntity>> getAllScoreRecords() {
        List<ScoreRecordEntity> scoreRecords = scoreRecordService.getAllScoreRecords();
        return ResponseEntity.ok(scoreRecords);
    }

    @PostMapping
    public ScoreRecordEntity createScoreRecord(@RequestBody ScoreRecordEntity scoreRecord) {
        return scoreRecordService.createScoreRecord(scoreRecord);
    }

    @PutMapping("/{scoreId}")
    public ResponseEntity<ScoreRecordEntity> updateScoreRecord(@PathVariable Long scoreId, @RequestBody ScoreRecordEntity updatedScoreRecord) {
        return scoreRecordService.updateScoreRecord(scoreId, updatedScoreRecord)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{scoreId}")
    public ResponseEntity<Void> deleteScoreRecord(@PathVariable Long scoreId) {
        scoreRecordService.deleteScoreRecord(scoreId);
        return ResponseEntity.noContent().build();
    }
}

