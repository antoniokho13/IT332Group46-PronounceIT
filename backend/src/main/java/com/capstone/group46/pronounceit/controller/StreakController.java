package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.dto.StreakDTO;
import com.capstone.group46.pronounceit.entity.StreakEntity;
import com.capstone.group46.pronounceit.service.StreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {
    @Autowired
    private StreakService streakService;

    @PostMapping("/{userId}")
    public ResponseEntity<StreakDTO> createStreak(@PathVariable Long userId) {
        StreakDTO streak = streakService.createStreakForUserDTO(userId);
        return ResponseEntity.ok(streak);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<StreakDTO> updateStreak(@PathVariable Long userId) {
        StreakDTO streak = streakService.updateStreakDTO(userId);
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
