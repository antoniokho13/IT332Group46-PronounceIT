package com.capstone.group46.pronounceit.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capstone.group46.pronounceit.dto.StreakDTO;
import com.capstone.group46.pronounceit.entity.StreakEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.repository.StreakRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;

@Service
public class StreakService {
    @Autowired
    private StreakRepository streakRepository;
    @Autowired
    private UserRepository userRepository;

    public StreakEntity createStreakForUser(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        UserEntity user = userOpt.get();
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only users with the STUDENT role can unlock a streak");
        }
        if (streakRepository.existsByUser(user)) throw new RuntimeException("Streak already exists for user");
        StreakEntity streak = new StreakEntity(user);
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        streak.setLastActivityDate(LocalDate.now());
        streak.setStreakStartDate(LocalDate.now());
        streak.setTotalActiveDays(1);
        return streakRepository.save(streak);
    }

    public StreakEntity updateStreak(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        UserEntity user = userOpt.get();
        Optional<StreakEntity> streakOpt = streakRepository.findByUser(user);
        if (streakOpt.isEmpty()) throw new RuntimeException("Streak not found for user");
        StreakEntity streak = streakOpt.get();
        LocalDate today = LocalDate.now();
        LocalDate last = streak.getLastActivityDate();
        if (last == null || last.isBefore(today.minusDays(1))) {
            // Missed a day, reset streak
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
            streak.setTotalActiveDays(1);
        } else if (last.isEqual(today.minusDays(1))) {
            // Consecutive day, increment streak
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }
        } // else if last.isEqual(today), do nothing (already updated today)
        streak.setLastActivityDate(today);
        return streakRepository.save(streak);
    }

    private StreakDTO toDTO(StreakEntity streak) {
        return new StreakDTO(
            streak.getUser().getId(),
            streak.getCurrentStreak(),
            streak.getLongestStreak(),
            streak.getLastActivityDate(),
            streak.getStreakStartDate(),
            streak.getTotalActiveDays()
        );
    }

    public StreakDTO getStreakForUser(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        UserEntity user = userOpt.get();
        Optional<StreakEntity> streakOpt = streakRepository.findByUser(user);
        if (streakOpt.isEmpty()) throw new RuntimeException("Streak not found for user");
        return toDTO(streakOpt.get());
    }

    public List<StreakDTO> getTopStreaks(int limit) {
        List<StreakEntity> topStreaks = streakRepository.findTop10LongestStreaks();
        return topStreaks.stream().limit(limit).map(this::toDTO).collect(Collectors.toList());
    }

    public StreakDTO createStreakForUserDTO(Long userId) {
        return toDTO(createStreakForUser(userId));
    }

    public StreakDTO updateStreakDTO(Long userId) {
        return toDTO(updateStreak(userId));
    }
}
