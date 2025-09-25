package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.AchievementEntity.TriggerType;
import com.capstone.group46.pronounceit.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*")
public class AchievementController {
    
    @Autowired
    private AchievementService achievementService;
    
    @GetMapping
    public ResponseEntity<List<AchievementEntity>> getAllAchievements() {
        List<AchievementEntity> achievements = achievementService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<AchievementEntity>> getActiveAchievements() {
        List<AchievementEntity> achievements = achievementService.getActiveAchievements();
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AchievementEntity> getAchievementById(@PathVariable Long id) {
        Optional<AchievementEntity> achievement = achievementService.getAchievementById(id);
        return achievement.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createAchievement(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("triggerType") TriggerType triggerType,
            @RequestParam(value = "triggerValue", required = false) Integer triggerValue,
            @RequestParam(value = "pointsReward", required = false) Integer pointsReward,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam(value = "badgeFile", required = false) MultipartFile badgeFile) {
        
        try {
            AchievementEntity achievement = new AchievementEntity();
            achievement.setTitle(title);
            achievement.setDescription(description);
            achievement.setTriggerType(triggerType);
            achievement.setTriggerValue(triggerValue);
            achievement.setPointsReward(pointsReward);
            achievement.setIsActive(isActive);
            
            AchievementEntity createdAchievement = achievementService.createAchievementWithBadge(achievement, badgeFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAchievement);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error uploading badge image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    
    @PostMapping("/json")
    public ResponseEntity<?> createAchievementJson(@RequestBody AchievementEntity achievement) {
        try {
            AchievementEntity createdAchievement = achievementService.createAchievement(achievement);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAchievement);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAchievement(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("triggerType") TriggerType triggerType,
            @RequestParam(value = "triggerValue", required = false) Integer triggerValue,
            @RequestParam(value = "pointsReward", required = false) Integer pointsReward,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam(value = "badgeFile", required = false) MultipartFile badgeFile) {
        
        try {
            AchievementEntity achievement = new AchievementEntity();
            achievement.setTitle(title);
            achievement.setDescription(description);
            achievement.setTriggerType(triggerType);
            achievement.setTriggerValue(triggerValue);
            achievement.setPointsReward(pointsReward);
            achievement.setIsActive(isActive);
            
            AchievementEntity updatedAchievement = achievementService.updateAchievementWithBadge(id, achievement, badgeFile);
            return ResponseEntity.ok(updatedAchievement);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error uploading badge image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    @PutMapping("/json/{id}")
    public ResponseEntity<?> updateAchievementJson(@PathVariable Long id, @RequestBody AchievementEntity achievement) {
        try {
            AchievementEntity updatedAchievement = achievementService.updateAchievement(id, achievement);
            return ResponseEntity.ok(updatedAchievement);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id) {
        try {
            achievementService.deleteAchievement(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleAchievementStatus(@PathVariable Long id) {
        try {
            achievementService.toggleAchievementStatus(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    @GetMapping("/trigger/{triggerType}")
    public ResponseEntity<List<AchievementEntity>> getAchievementsByTriggerType(@PathVariable TriggerType triggerType) {
        List<AchievementEntity> achievements = achievementService.getAchievementsByTriggerType(triggerType);
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AchievementEntity>> searchAchievements(@RequestParam String title) {
        List<AchievementEntity> achievements = achievementService.searchAchievementsByTitle(title);
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/trigger-types")
    public ResponseEntity<TriggerType[]> getAllTriggerTypes() {
        return ResponseEntity.ok(TriggerType.values());
    }
}
