package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
            achievement.setPointsRequired(updatedAchievement.getPointsRequired());
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
            achievement.setPointsRequired(updatedAchievement.getPointsRequired());
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
    
    public List<AchievementEntity> searchAchievementsByTitle(String title) {
        return achievementRepository.findByTitleContaining(title);
    }
    
    // Method to get achievements that a user can unlock based on their accumulated points
    public List<AchievementEntity> getEligibleAchievements(Integer userPoints) {
        return achievementRepository.findEligibleAchievementsByPoints(userPoints);
    }
    
    // Method to get achievements ordered by points required (ascending)
    public List<AchievementEntity> getAchievementsOrderedByPoints() {
        return achievementRepository.findAllByOrderByPointsRequiredAsc();
    }

    // Method to check and unlock achievements for a user based on their points
    public List<AchievementEntity> checkAndUnlockAchievements(UserEntity user) {
        List<AchievementEntity> newlyUnlocked = new ArrayList<>();
        List<AchievementEntity> eligibleAchievements = getEligibleAchievements(user.getAccumulatedPoints());
        
        for (AchievementEntity achievement : eligibleAchievements) {
            // Check if user doesn't already have this achievement
            if (!user.getAchievements().contains(achievement)) {
                user.getAchievements().add(achievement);
                newlyUnlocked.add(achievement);
            }
        }
        
        return newlyUnlocked;
    }

    // Method to get achievements not yet unlocked by a specific user
    public List<AchievementEntity> getAvailableAchievements(UserEntity user) {
        List<AchievementEntity> allActiveAchievements = getActiveAchievements();
        List<AchievementEntity> userAchievements = user.getAchievements();
        
        return allActiveAchievements.stream()
            .filter(achievement -> !userAchievements.contains(achievement))
            .collect(Collectors.toList());
    }

    // Method to get next achievement user can work towards
    public Optional<AchievementEntity> getNextAchievement(UserEntity user) {
        return getAvailableAchievements(user).stream()
            .filter(achievement -> achievement.getPointsRequired() > user.getAccumulatedPoints())
            .min(Comparator.comparing(AchievementEntity::getPointsRequired));
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
