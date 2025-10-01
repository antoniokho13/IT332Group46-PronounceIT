package com.capstone.group46.pronounceit.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.group46.pronounceit.dto.StreakDTO;
import com.capstone.group46.pronounceit.service.StreakService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {
    private static final Logger logger = LoggerFactory.getLogger(StreakController.class);
    @Autowired
    private StreakService streakService;

    @PostMapping("/{userId}")
    public ResponseEntity<StreakDTO> createStreak(@PathVariable Long userId,
                                                  @RequestParam(required = false) Boolean mark,
                                                  @RequestParam(required = false) String date) {
        // If caller requests to create and immediately mark activity, delegate to helper
        if (mark != null && mark.booleanValue()) {
            java.time.LocalDate activityDate = null;
            if (date != null && !date.isBlank()) {
                activityDate = java.time.LocalDate.parse(date);
            } else {
                activityDate = java.time.LocalDate.now();
            }
            StreakDTO streak = streakService.updateStreakOnActivityDTO(userId, activityDate);
            return ResponseEntity.ok(streak);
        }

        StreakDTO streak = streakService.createStreakForUserDTO(userId);
        return ResponseEntity.ok(streak);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<StreakDTO> updateStreak(@PathVariable Long userId) {
        StreakDTO streak = streakService.updateStreakDTO(userId);
        return ResponseEntity.ok(streak);
    }

    /**
     * Mark activity (e.g. lesson completion) for a given user. Optional ISO date may be supplied
     * as a query parameter `date=YYYY-MM-DD`. If omitted, server date (now) is used.
     */
    @PostMapping("/{userId}/activity")
    public ResponseEntity<StreakDTO> markActivity(@PathVariable Long userId, @RequestParam(required = false) String date, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.info("Received streak activity mark request for userId={} date={} Authorization={}", userId, date, authHeader != null ? "present" : "absent");

        java.time.LocalDate activityDate = null;
        if (date != null && !date.isBlank()) {
            activityDate = java.time.LocalDate.parse(date);
        } else {
            activityDate = java.time.LocalDate.now();
        }
        StreakDTO streak = streakService.updateStreakOnActivityDTO(userId, activityDate);
        logger.info("Updated streak for userId={} -> current={}, longest={}, lastActivity={}", streak.getUserId(), streak.getCurrentStreak(), streak.getLongestStreak(), streak.getLastActivityDate());
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<StreakDTO> getStreak(@PathVariable Long userId) {
        StreakDTO streak = streakService.getStreakForUser(userId);
        return ResponseEntity.ok(streak);
    }

    @GetMapping("/top")
    public ResponseEntity<List<StreakDTO>> getTopStreaks(@RequestParam(defaultValue = "10") int limit) {
        List<StreakDTO> topStreaks = streakService.getTopStreaks(limit);
        return ResponseEntity.ok(topStreaks);
    }
}
