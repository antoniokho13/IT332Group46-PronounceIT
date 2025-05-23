package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;
import com.capstone.group46.pronounceit.repository.PronounciationAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PronounciationAttemptService {
    private final PronounciationAttemptRepository pronounciationAttemptRepository;

    public PronounciationAttemptService(PronounciationAttemptRepository pronounciationAttemptRepository) {
        this.pronounciationAttemptRepository = pronounciationAttemptRepository;
    }

    public List<PronounciationAttemptEntity> getAllPronounciationAttempts() {
        return pronounciationAttemptRepository.findAll();
    }

    public Optional<PronounciationAttemptEntity> getPronounciationAttemptById(Long attemptId) {
        return pronounciationAttemptRepository.findById(attemptId);
    }

    public PronounciationAttemptEntity createPronounciationAttempt(PronounciationAttemptEntity pronounciationAttempt) {
        return pronounciationAttemptRepository.save(pronounciationAttempt);
    }

    public Optional<PronounciationAttemptEntity> updatePronounciationAttempt(Long attemptId, PronounciationAttemptEntity updatedPronounciationAttempt) {
        return pronounciationAttemptRepository.findById(attemptId).map(pronounciationAttempt -> {
            pronounciationAttempt.setAccuracy(updatedPronounciationAttempt.getAccuracy());
            pronounciationAttempt.setCorrect(updatedPronounciationAttempt.isCorrect());
            pronounciationAttempt.setAttemptNumber(updatedPronounciationAttempt.getAttemptNumber());
            return pronounciationAttemptRepository.save(pronounciationAttempt);
        });
    }

    public void deletePronounciationAttempt(Long attemptId) {
        pronounciationAttemptRepository.deleteById(attemptId);
    }

    public int countAttemptsForUserWordLesson(Long userId, Long wordId, Long lessonId) {
        return pronounciationAttemptRepository.countByUser_IdAndWord_WordIdAndLesson_LessonId(userId, wordId, lessonId);
    }

    public Optional<PronounciationAttemptEntity> findByUserAndWordAndLesson(Long userId, Long wordId, Long lessonId) {
        return pronounciationAttemptRepository.findByUser_IdAndWord_WordIdAndLesson_LessonId(userId, wordId, lessonId);
    }

    public Optional<PronounciationAttemptEntity> findByUserWordLessonSession(
        Long userId, Long wordId, Long lessonId, String sessionId
    ) {
        return pronounciationAttemptRepository.findByUser_IdAndWord_WordIdAndLesson_LessonIdAndSessionId(
            userId, wordId, lessonId, sessionId
        );
    }

    public List<PronounciationAttemptEntity> findAllByUserLessonSession(Long userId, Long lessonId, String sessionId) {
        return pronounciationAttemptRepository.findAllByUser_IdAndLesson_LessonIdAndSessionId(userId, lessonId, sessionId);
    }
}