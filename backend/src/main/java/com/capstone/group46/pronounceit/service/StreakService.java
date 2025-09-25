package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.StreakEntity;
import com.capstone.group46.pronounceit.repository.StreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class StreakService {
    
    @Autowired
    private StreakRepository streakRepository;
    
    public StreakEntity getUserStreak(Long userId) {
        Optional<StreakEntity> streak = streakRepository.findByUserId(userId);
        if (streak.isPresent()) {
            return streak.get();
        } else {
            // Create new streak for user
            StreakEntity newStreak = new StreakEntity(userId);
            return streakRepository.save(newStreak);
        }
    }
    
    public StreakEntity updateUserActivity(Long userId) {
        StreakEntity streak = getUserStreak(userId);
        LocalDate today = LocalDate.now();
        
        // If user already did activity today, don't update
        if (streak.getLastActivityDate() != null && streak.getLastActivityDate().equals(today)) {
            return streak;
        }
        
        // Check if streak should continue or reset
        if (streak.getLastActivityDate() == null) {
            // First time user activity
            startNewStreak(streak, today);
        } else {
            long daysSinceLastActivity = ChronoUnit.DAYS.between(streak.getLastActivityDate(), today);
            
            if (daysSinceLastActivity == 1) {
                // Continue streak - consecutive day
                continueStreak(streak, today);
            } else if (daysSinceLastActivity > 1) {
                // Reset streak - missed days
                resetAndStartNewStreak(streak, today);
            }
            // If daysSinceLastActivity == 0, it means same day - already handled above
        }
        
        return streakRepository.save(streak);
    }
    
    private void startNewStreak(StreakEntity streak, LocalDate today) {
        streak.setCurrentStreak(1);
        streak.setLastActivityDate(today);
        streak.setStreakStartDate(today);
        streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
        
        // Update longest streak if this is the first streak or if current becomes longest
        if (streak.getLongestStreak() < 1) {
            streak.setLongestStreak(1);
        }
    }
    
    private void continueStreak(StreakEntity streak, LocalDate today) {
        streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        streak.setLastActivityDate(today);
        streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
        
        // Update longest streak if current streak is now the longest
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
    }
    
    private void resetAndStartNewStreak(StreakEntity streak, LocalDate today) {
        streak.setCurrentStreak(1);
        streak.setLastActivityDate(today);
        streak.setStreakStartDate(today);
        streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
    }
    
    public void resetUserStreak(Long userId) {
        Optional<StreakEntity> streakOpt = streakRepository.findByUserId(userId);
        if (streakOpt.isPresent()) {
            StreakEntity streak = streakOpt.get();
            streak.setCurrentStreak(0);
            streak.setStreakStartDate(null);
            streakRepository.save(streak);
        }
    }
    
    public List<StreakEntity> getActiveStreaks() {
        return streakRepository.findActiveStreaksOrderByStreakDesc();
    }
    
    public List<StreakEntity> getTopLongestStreaks() {
        return streakRepository.findTop10LongestStreaks();
    }
    
    public Double getAverageActiveStreak() {
        return streakRepository.findAverageActiveStreak();
    }
    
    public Long getUsersWithStreakAtLeast(Integer days) {
        return streakRepository.countUsersWithStreakAtLeast(days);
    }
    
    // Scheduled task to reset streaks for users who missed days
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    public void resetInactiveStreaks() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<StreakEntity> streaksToReset = streakRepository.findStreaksToReset(yesterday);
        
        for (StreakEntity streak : streaksToReset) {
            long daysSinceLastActivity = ChronoUnit.DAYS.between(streak.getLastActivityDate(), LocalDate.now());
            
            if (daysSinceLastActivity > 1) {
                streak.setCurrentStreak(0);
                streak.setStreakStartDate(null);
                streakRepository.save(streak);
            }
        }
    }
    
    public boolean isStreakActive(Long userId) {
        StreakEntity streak = getUserStreak(userId);
        if (streak.getLastActivityDate() == null || streak.getCurrentStreak() == 0) {
            return false;
        }
        
        long daysSinceLastActivity = ChronoUnit.DAYS.between(streak.getLastActivityDate(), LocalDate.now());
        return daysSinceLastActivity <= 1; // Active if did activity today or yesterday
    }
    
    public int getDaysUntilStreakReset(Long userId) {
        StreakEntity streak = getUserStreak(userId);
        if (streak.getLastActivityDate() == null) {
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        long daysSinceLastActivity = ChronoUnit.DAYS.between(streak.getLastActivityDate(), today);
        
        if (daysSinceLastActivity == 0) {
            return 1; // Has until tomorrow
        } else if (daysSinceLastActivity == 1) {
            return 0; // Must do activity today or streak resets
        } else {
            return -1; // Streak already broken
        }
    }
}
