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
}