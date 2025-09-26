package com.capstone.group46.pronounceit.dto;

import java.time.LocalDate;

public class StreakDTO {
    private Long userId;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;
    private LocalDate streakStartDate;
    private int totalActiveDays;

    public StreakDTO() {}

    public StreakDTO(Long userId, int currentStreak, int longestStreak, LocalDate lastActivityDate, LocalDate streakStartDate, int totalActiveDays) {
        this.userId = userId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActivityDate = lastActivityDate;
        this.streakStartDate = streakStartDate;
        this.totalActiveDays = totalActiveDays;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public LocalDate getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }
    public LocalDate getStreakStartDate() { return streakStartDate; }
    public void setStreakStartDate(LocalDate streakStartDate) { this.streakStartDate = streakStartDate; }
    public int getTotalActiveDays() { return totalActiveDays; }
    public void setTotalActiveDays(int totalActiveDays) { this.totalActiveDays = totalActiveDays; }
}

