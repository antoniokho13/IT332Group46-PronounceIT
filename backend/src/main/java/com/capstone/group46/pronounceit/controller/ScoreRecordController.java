package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.dto.ScoreRecordDTO;
import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.entity.ScoreRecordEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.service.ScoreRecordService;
import com.capstone.group46.pronounceit.service.UserService;
import com.capstone.group46.pronounceit.service.LessonService;
// import com.google.common.base.Optional; // Removed, use java.util.Optional instead
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/score-records")
public class ScoreRecordController {
    private final ScoreRecordService scoreRecordService;
    private final UserService userService;
    private final LessonService lessonService;

    public ScoreRecordController(ScoreRecordService scoreRecordService, UserService userService, LessonService lessonService) {
        this.scoreRecordService = scoreRecordService;
        this.userService = userService;
        this.lessonService = lessonService;
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

    @PostMapping("/save-session-score")
    public ScoreRecordEntity saveOrUpdateScore(
        @RequestBody ScoreRecordDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity user = userService.findByEmail(userDetails.getUsername());
        LessonEntity lesson = lessonService.getLessonById(dto.lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Optional<ScoreRecordEntity> existing = scoreRecordService.findByUserLessonSession(user.getId(), lesson.getLessonId(), dto.sessionId);

        ScoreRecordEntity scoreRecord = existing.orElseGet(ScoreRecordEntity::new);
        scoreRecord.setUser(user);
        scoreRecord.setLesson(lesson);
        scoreRecord.setCategory(lesson.getCategory());
        scoreRecord.setScore(dto.score);
        scoreRecord.setCompletionDate(LocalDateTime.now());
        scoreRecord.setAttemptsDuration(dto.attemptsDuration);
        scoreRecord.setCorrectWords(dto.correctWords);
        scoreRecord.setIncorrectWords(dto.incorrectWords);
        scoreRecord.setSessionId(dto.sessionId);

        return scoreRecordService.createScoreRecord(scoreRecord);
    }
}

