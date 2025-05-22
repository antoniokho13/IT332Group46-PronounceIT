package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.dto.PronounciationAttemptPostDTO;
import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.service.PronounciationAttemptService;
import com.capstone.group46.pronounceit.service.UserService;
import com.capstone.group46.pronounceit.service.WordService;
import com.capstone.group46.pronounceit.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pronounciation-attempts")
public class PronounciationAttemptController {
    private final PronounciationAttemptService pronounciationAttemptService;
    private final UserService userService;
    private final WordService wordService;
    private final LessonService lessonService;

    public PronounciationAttemptController(
            PronounciationAttemptService pronounciationAttemptService,
            UserService userService,
            WordService wordService,
            LessonService lessonService
    ) {
        this.pronounciationAttemptService = pronounciationAttemptService;
        this.userService = userService;
        this.wordService = wordService;
        this.lessonService = lessonService;
    }

    @PostMapping
    public PronounciationAttemptEntity createOrUpdatePronounciationAttempt(
            @RequestBody PronounciationAttemptPostDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity userEntity = userService.findByEmail(userDetails.getUsername());
        WordEntity word = wordService.getWordById(dto.wordId)
            .orElseThrow(() -> new RuntimeException("Word not found"));
        LessonEntity lesson = lessonService.getLessonById(dto.lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Optional<PronounciationAttemptEntity> existingAttemptOpt =
            pronounciationAttemptService.findByUserWordLessonSession(
                userEntity.getId(), word.getWordId(), lesson.getLessonId(), dto.sessionId
            );

        PronounciationAttemptEntity attempt = existingAttemptOpt.orElseGet(PronounciationAttemptEntity::new);
        attempt.setUser(userEntity);
        attempt.setWord(word);
        attempt.setLesson(lesson);
        attempt.setAccuracy(dto.accuracy);
        attempt.setCorrect(dto.isCorrect);
        attempt.setAttemptNumber(dto.attemptNumber);
        attempt.setSessionId(dto.sessionId);

        return pronounciationAttemptService.createPronounciationAttempt(attempt);
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

    @PutMapping("/{attemptId}")
    public ResponseEntity<PronounciationAttemptEntity> updatePronounciationAttempt(
            @PathVariable Long attemptId,
            @RequestBody PronounciationAttemptEntity updatedPronounciationAttempt
    ) {
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