package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.AchievementEntity.TriggerType;
import com.capstone.group46.pronounceit.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AchievementService {
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    private static final String BADGE_UPLOAD_DIR = "src/main/resources/images/badges/";
    
    public List<AchievementEntity> getAllAchievements() {
        return achievementRepository.findAll();
    }
    
    public List<AchievementEntity> getActiveAchievements() {
        return achievementRepository.findByIsActiveTrue();
    }
    
    public Optional<AchievementEntity> getAchievementById(Long id) {
        return achievementRepository.findById(id);
    }
    
    public AchievementEntity createAchievement(AchievementEntity achievement) {
        if (achievementRepository.existsByTitle(achievement.getTitle())) {
            throw new RuntimeException("Achievement with this title already exists");
        }
        return achievementRepository.save(achievement);
    }
    
    public AchievementEntity createAchievementWithBadge(AchievementEntity achievement, MultipartFile badgeFile) throws IOException {
        if (achievementRepository.existsByTitle(achievement.getTitle())) {
            throw new RuntimeException("Achievement with this title already exists");
        }
        
        if (badgeFile != null && !badgeFile.isEmpty()) {
            String badgeImagePath = saveBadgeImage(badgeFile);
            achievement.setBadgeImagePath(badgeImagePath);
        }
        
        return achievementRepository.save(achievement);
    }
    
    public AchievementEntity updateAchievement(Long id, AchievementEntity updatedAchievement) {
        Optional<AchievementEntity> existingAchievement = achievementRepository.findById(id);
        if (existingAchievement.isPresent()) {
            AchievementEntity achievement = existingAchievement.get();
            achievement.setTitle(updatedAchievement.getTitle());
            achievement.setDescription(updatedAchievement.getDescription());
            achievement.setTriggerType(updatedAchievement.getTriggerType());
            achievement.setTriggerValue(updatedAchievement.getTriggerValue());
            achievement.setPointsReward(updatedAchievement.getPointsReward());
            achievement.setIsActive(updatedAchievement.getIsActive());
            
            if (updatedAchievement.getBadgeImagePath() != null) {
                achievement.setBadgeImagePath(updatedAchievement.getBadgeImagePath());
            }
            
            return achievementRepository.save(achievement);
        }
        throw new RuntimeException("Achievement not found with id: " + id);
    }
    
    public AchievementEntity updateAchievementWithBadge(Long id, AchievementEntity updatedAchievement, MultipartFile badgeFile) throws IOException {
        Optional<AchievementEntity> existingAchievement = achievementRepository.findById(id);
        if (existingAchievement.isPresent()) {
            AchievementEntity achievement = existingAchievement.get();
            achievement.setTitle(updatedAchievement.getTitle());
            achievement.setDescription(updatedAchievement.getDescription());
            achievement.setTriggerType(updatedAchievement.getTriggerType());
            achievement.setTriggerValue(updatedAchievement.getTriggerValue());
            achievement.setPointsReward(updatedAchievement.getPointsReward());
            achievement.setIsActive(updatedAchievement.getIsActive());
            
            if (badgeFile != null && !badgeFile.isEmpty()) {
                String badgeImagePath = saveBadgeImage(badgeFile);
                achievement.setBadgeImagePath(badgeImagePath);
            }
            
            return achievementRepository.save(achievement);
        }
        throw new RuntimeException("Achievement not found with id: " + id);
    }
    
    public void deleteAchievement(Long id) {
        if (achievementRepository.existsById(id)) {
            achievementRepository.deleteById(id);
        } else {
            throw new RuntimeException("Achievement not found with id: " + id);
        }
    }
    
    public void toggleAchievementStatus(Long id) {
        Optional<AchievementEntity> achievement = achievementRepository.findById(id);
        if (achievement.isPresent()) {
            AchievementEntity entity = achievement.get();
            entity.setIsActive(!entity.getIsActive());
            achievementRepository.save(entity);
        } else {
            throw new RuntimeException("Achievement not found with id: " + id);
        }
    }
    
    public List<AchievementEntity> getAchievementsByTriggerType(TriggerType triggerType) {
        return achievementRepository.findByTriggerTypeAndIsActiveTrue(triggerType);
    }
    
    public List<AchievementEntity> searchAchievementsByTitle(String title) {
        return achievementRepository.findByTitleContaining(title);
    }
    
    // Method to check if user is eligible for achievements (will be used later with trigger logic)
    public List<AchievementEntity> getEligibleAchievements(TriggerType triggerType, Integer value) {
        return achievementRepository.findEligibleAchievements(triggerType, value);
    }
    
    private String saveBadgeImage(MultipartFile file) throws IOException {
        // Create badges directory if it doesn't exist
        Path uploadPath = Paths.get(BADGE_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
        String uniqueFilename = UUID.randomUUID().toString() + "_" + 
            (originalFilename != null ? originalFilename.replaceFirst("[.][^.]+$", "") : "badge") + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        return "images/badges/" + uniqueFilename;
    }
}
