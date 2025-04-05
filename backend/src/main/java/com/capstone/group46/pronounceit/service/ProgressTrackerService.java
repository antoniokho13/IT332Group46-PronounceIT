package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.ProgressTrackerEntity;
import com.capstone.group46.pronounceit.repository.ProgressTrackerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgressTrackerService {
    private final ProgressTrackerRepository progressTrackerRepository;

    public ProgressTrackerService(ProgressTrackerRepository progressTrackerRepository) {
        this.progressTrackerRepository = progressTrackerRepository;
    }

    public List<ProgressTrackerEntity> getAllProgressTrackers() {
        return progressTrackerRepository.findAll();
    }

    public Optional<ProgressTrackerEntity> getProgressTrackerById(Long progressId) {
        return progressTrackerRepository.findById(progressId);
    }

    public ProgressTrackerEntity createProgressTracker(ProgressTrackerEntity progressTracker) {
        return progressTrackerRepository.save(progressTracker);
    }

    public Optional<ProgressTrackerEntity> updateProgressTracker(Long progressId, ProgressTrackerEntity updatedProgressTracker) {
        return progressTrackerRepository.findById(progressId).map(progressTracker -> {
            progressTracker.setStatus(updatedProgressTracker.getStatus());
            progressTracker.setLastAttemptDate(updatedProgressTracker.getLastAttemptDate());
            progressTracker.setCompletionDate(updatedProgressTracker.getCompletionDate());
            progressTracker.setUnlocked(updatedProgressTracker.isUnlocked());
            return progressTrackerRepository.save(progressTracker);
        });
    }

    public void deleteProgressTracker(Long progressId) {
        progressTrackerRepository.deleteById(progressId);
    }
}