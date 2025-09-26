package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.service.AchievementService;
import com.capstone.group46.pronounceit.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AchievementService achievementService;
    
    public UserController(UserService userService, AchievementService achievementService){
        this.userService = userService;
        this.achievementService = achievementService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public UserEntity createUser(@RequestBody UserEntity user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody UserEntity updatedUser) {
        return userService.updateUser(id, updatedUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserEntity> patchUser(@PathVariable Long id, @RequestBody UserEntity updatedUser) {
        return userService.updateUser(id, updatedUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ========= Achievement-related endpoints ========= //

    @GetMapping("/{id}/achievements")
    public ResponseEntity<List<AchievementEntity>> getUserAchievements(@PathVariable Long id) {
        try {
            List<AchievementEntity> achievements = userService.getUserAchievements(id);
            return ResponseEntity.ok(achievements);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/achievements/{achievementId}")
    public ResponseEntity<?> unlockAchievement(@PathVariable Long userId, @PathVariable Long achievementId) {
        try {
            Optional<AchievementEntity> achievement = achievementService.getAchievementById(achievementId);
            if (achievement.isPresent()) {
                UserEntity updatedUser = userService.unlockAchievement(userId, achievement.get());
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.badRequest().body("Achievement not found");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/achievements/available")
    public ResponseEntity<List<AchievementEntity>> getAvailableAchievements(@PathVariable Long id) {
        try {
            Optional<UserEntity> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                List<AchievementEntity> availableAchievements = achievementService.getAvailableAchievements(userOpt.get());
                return ResponseEntity.ok(availableAchievements);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/achievements/next")
    public ResponseEntity<AchievementEntity> getNextAchievement(@PathVariable Long id) {
        try {
            Optional<UserEntity> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                Optional<AchievementEntity> nextAchievement = achievementService.getNextAchievement(userOpt.get());
                return nextAchievement.map(ResponseEntity::ok)
                                    .orElse(ResponseEntity.noContent().build());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/check-achievements")
    public ResponseEntity<List<AchievementEntity>> checkAndUnlockAchievements(@PathVariable Long id) {
        try {
            Optional<UserEntity> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                List<AchievementEntity> newlyUnlocked = achievementService.checkAndUnlockAchievements(userOpt.get());
                return ResponseEntity.ok(newlyUnlocked);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}