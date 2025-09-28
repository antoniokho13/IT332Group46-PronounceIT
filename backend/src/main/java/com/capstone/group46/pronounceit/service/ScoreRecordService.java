package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.ScoreRecordEntity;
import com.capstone.group46.pronounceit.repository.ScoreRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreRecordService {
    private final ScoreRecordRepository scoreRecordRepository;

    public ScoreRecordService(ScoreRecordRepository scoreRecordRepository) {
        this.scoreRecordRepository = scoreRecordRepository;
    }

    public List<ScoreRecordEntity> getAllScoreRecords() {
        return scoreRecordRepository.findAll();
    }

    public Optional<ScoreRecordEntity> getScoreRecordById(Long scoreId) {
        return scoreRecordRepository.findById(scoreId);
    }

    public ScoreRecordEntity createScoreRecord(ScoreRecordEntity scoreRecord) {
        return scoreRecordRepository.save(scoreRecord);
    }

    public Optional<ScoreRecordEntity> updateScoreRecord(Long scoreId, ScoreRecordEntity updatedScoreRecord) {
        return scoreRecordRepository.findById(scoreId).map(scoreRecord -> {
            scoreRecord.setScore(updatedScoreRecord.getScore());
            scoreRecord.setCompletionDate(updatedScoreRecord.getCompletionDate());
            scoreRecord.setAttemptsDuration(updatedScoreRecord.getAttemptsDuration());
            scoreRecord.setCorrectWords(updatedScoreRecord.getCorrectWords());
            scoreRecord.setIncorrectWords(updatedScoreRecord.getIncorrectWords());
            return scoreRecordRepository.save(scoreRecord);
        });
    }

    public void deleteScoreRecord(Long scoreId) {
        scoreRecordRepository.deleteById(scoreId);
    }

    public Optional<ScoreRecordEntity> findByUserLessonSession(Long userId, Long lessonId, String sessionId) {
        return scoreRecordRepository.findByUser_IdAndLesson_LessonIdAndSessionId(userId, lessonId, sessionId);
    }

    public Optional<ScoreRecordEntity> findLatestByUserAndLesson(Long userId, Long lessonId) {
        List<ScoreRecordEntity> records = scoreRecordRepository.findTop1ByUser_IdAndLesson_LessonIdOrderByCompletionDateDesc(userId, lessonId);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }

    /**
     * Returns the best (highest) score record for a user on a specific lesson, if any.
     */
    public Optional<ScoreRecordEntity> findBestByUserAndLesson(Long userId, Long lessonId) {
        return scoreRecordRepository.findTop1ByUser_IdAndLesson_LessonIdOrderByScoreDesc(userId, lessonId);
    }

    /**
     * Returns the best score for the specified user and lesson excluding the provided sessionId.
     * This helps compute the previous best when updating or creating a record for the same session.
     */
    public Optional<ScoreRecordEntity> findBestByUserAndLessonExcludingSession(Long userId, Long lessonId, String sessionId) {
        // Fetch top 2 records by score. If the top record belongs to the same sessionId, consider the second one as previous best.
        List<ScoreRecordEntity> topRecords = scoreRecordRepository.findTop2ByUser_IdAndLesson_LessonIdOrderByScoreDesc(userId, lessonId);
        if (topRecords.isEmpty()) return Optional.empty();
        if (topRecords.size() == 1) {
            return topRecords.get(0).getSessionId().equals(sessionId) ? Optional.empty() : Optional.of(topRecords.get(0));
        }
        // size >= 2
        ScoreRecordEntity first = topRecords.get(0);
        ScoreRecordEntity second = topRecords.get(1);
        if (!first.getSessionId().equals(sessionId)) {
            return Optional.of(first);
        } else if (!second.getSessionId().equals(sessionId)) {
            return Optional.of(second);
        }
        return Optional.empty();
    }

    /**
     * Save or update a score record (delegates to repository save). Kept for clarity.
     */
    public ScoreRecordEntity save(ScoreRecordEntity scoreRecord) {
        return scoreRecordRepository.save(scoreRecord);
    }
}