package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.StreakEntity;
import com.capstone.group46.pronounceit.service.StreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/streaks")
@CrossOrigin(origins = "*")
public class StreakController {
    
    @Autowired
    private StreakService streakService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<StreakEntity> getUserStreak(@PathVariable Long userId) {
        StreakEntity streak = streakService.getUserStreak(userId);
        return ResponseEntity.ok(streak);
    }
    
    @PostMapping("/user/{userId}/activity")
    public ResponseEntity<StreakEntity> recordUserActivity(@PathVariable Long userId) {
        StreakEntity updatedStreak = streakService.updateUserActivity(userId);
        return ResponseEntity.ok(updatedStreak);
    }
    
    @PostMapping("/user/{userId}/reset")
    public ResponseEntity<String> resetUserStreak(@PathVariable Long userId) {
        streakService.resetUserStreak(userId);
        return ResponseEntity.ok("Streak reset successfully");
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<StreakEntity>> getActiveStreaks() {
        List<StreakEntity> activeStreaks = streakService.getActiveStreaks();
        return ResponseEntity.ok(activeStreaks);
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<StreakEntity>> getStreakLeaderboard() {
        List<StreakEntity> topStreaks = streakService.getTopLongestStreaks();
        return ResponseEntity.ok(topStreaks);
    }
    
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Map<String, Object>> getUserStreakStatus(@PathVariable Long userId) {
        StreakEntity streak = streakService.getUserStreak(userId);
        boolean isActive = streakService.isStreakActive(userId);
        int daysUntilReset = streakService.getDaysUntilStreakReset(userId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("streak", streak);
        status.put("isActive", isActive);
        status.put("daysUntilReset", daysUntilReset);
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStreakStatistics() {
        Double averageStreak = streakService.getAverageActiveStreak();
        Long users7DayStreak = streakService.getUsersWithStreakAtLeast(7);
        Long users30DayStreak = streakService.getUsersWithStreakAtLeast(30);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageActiveStreak", averageStreak != null ? averageStreak : 0.0);
        stats.put("usersWithWeekStreak", users7DayStreak);
        stats.put("usersWithMonthStreak", users30DayStreak);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/user/{userId}/motivation")
    public ResponseEntity<Map<String, String>> getMotivationalMessage(@PathVariable Long userId) {
        StreakEntity streak = streakService.getUserStreak(userId);
        Map<String, String> response = new HashMap<>();
        
        String message;
        if (streak.getCurrentStreak() == 0) {
            message = "Start your learning journey today! 🌟";
        } else if (streak.getCurrentStreak() == 1) {
            message = "Great start! Keep it going tomorrow! 🔥";
        } else if (streak.getCurrentStreak() < 7) {
            message = "You're on fire! " + streak.getCurrentStreak() + " days strong! 🚀";
        } else if (streak.getCurrentStreak() < 30) {
            message = "Amazing dedication! " + streak.getCurrentStreak() + " days of learning! 🏆";
        } else {
            message = "Incredible! " + streak.getCurrentStreak() + " days streak! You're a learning champion! 👑";
        }
        
        response.put("message", message);
        response.put("streakCount", String.valueOf(streak.getCurrentStreak()));
        
        return ResponseEntity.ok(response);
    }
}
