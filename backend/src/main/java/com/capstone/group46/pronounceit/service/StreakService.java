package com.capstone.group46.pronounceit.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        // Create an initial, inactive streak. Do not mark an activity during creation.
        // The first lesson completion should trigger an update which sets currentStreak = 1.
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        streak.setLastActivityDate(null);
        streak.setStreakStartDate(null);
        streak.setTotalActiveDays(0);
        return streakRepository.save(streak);
    }

    public StreakEntity updateStreak(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        // Delegate to date-aware updater using today's date
        return updateStreakOnActivity(userId, LocalDate.now());
    }

    /**
     * Update streak using a supplied activity date (useful when activity occurs in another component
     * and the timestamp should be applied instead of server-now).
     */
    public StreakEntity updateStreakOnActivity(Long userId, LocalDate activityDate) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        UserEntity user = userOpt.get();
        Optional<StreakEntity> streakOpt = streakRepository.findByUser(user);
        StreakEntity streak;
        if (streakOpt.isEmpty()) {
            // Create and persist an initial streak record for the user (inactive) so activity can be applied
            streak = new StreakEntity(user);
            streak.setCurrentStreak(0);
            streak.setLongestStreak(0);
            streak.setLastActivityDate(null);
            streak.setStreakStartDate(null);
            streak.setTotalActiveDays(0);
            streak = streakRepository.save(streak);
        } else {
            streak = streakOpt.get();
        }

        LocalDate last = streak.getLastActivityDate();

        // Use primitive locals to avoid unboxing nullable Integer values from the entity
    Integer currentObj = streak.getCurrentStreak();
    Integer longestObj = streak.getLongestStreak();
    Integer totalActiveObj = streak.getTotalActiveDays();
    int current = currentObj == null ? 0 : currentObj.intValue();
    int longest = longestObj == null ? 0 : longestObj.intValue();
    int totalActive = totalActiveObj == null ? 0 : totalActiveObj.intValue();

        if (last == null || last.isBefore(activityDate.minusDays(1))) {
            // Missed a day (or first activity), reset/start streak
            current = 1;
            longest = Math.max(longest, current);
            totalActive = totalActive + 1;
            streak.setStreakStartDate(activityDate);
        } else if (last.isEqual(activityDate.minusDays(1))) {
            // Consecutive day, increment streak
            current = current + 1;
            totalActive = totalActive + 1;
            if (current > longest) {
                longest = current;
            }
        } else if (last.isEqual(activityDate)) {
            // Already updated for this date; do nothing
        } else if (last.isAfter(activityDate)) {
            // Activity date is before last recorded activity: ignore to prevent regressions
        }

        // Persist calculated values back into the entity
        streak.setCurrentStreak(current);
        streak.setLongestStreak(longest);
        streak.setTotalActiveDays(totalActive);
        streak.setLastActivityDate(activityDate);

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
    return topStreaks.stream().limit(limit).map(this::toDTO).toList();
    }

    public StreakDTO createStreakForUserDTO(Long userId) {
        return toDTO(createStreakForUser(userId));
    }

    // Create a streak and immediately mark it as active with the supplied activity date
    public StreakEntity createStreakAndMark(Long userId, LocalDate activityDate) {
        try {
            createStreakForUser(userId);
        } catch (RuntimeException ex) {
            // ignore if streak already exists; we'll update it below
        }
        return updateStreakOnActivity(userId, activityDate);
    }

    public StreakDTO updateStreakDTO(Long userId) {
        return toDTO(updateStreak(userId));
    }

    // Public wrapper that updates streak using supplied activity date and returns DTO
    public StreakDTO updateStreakOnActivityDTO(Long userId, LocalDate activityDate) {
        return toDTO(updateStreakOnActivity(userId, activityDate));
    }
}
