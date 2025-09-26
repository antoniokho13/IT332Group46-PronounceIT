package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.StreakEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<StreakEntity, Long> {
    Optional<StreakEntity> findByUser(UserEntity user);

    @Query("SELECT s FROM StreakEntity s WHERE s.currentStreak > 0 ORDER BY s.currentStreak DESC")
    List<StreakEntity> findActiveStreaksOrderByStreakDesc();

    @Query("SELECT s FROM StreakEntity s ORDER BY s.longestStreak DESC LIMIT 10")
    List<StreakEntity> findTop10LongestStreaks();

    @Query("SELECT s FROM StreakEntity s WHERE s.lastActivityDate < :date AND s.currentStreak > 0")
    List<StreakEntity> findStreaksToReset(@Param("date") LocalDate date);

    @Query("SELECT AVG(s.currentStreak) FROM StreakEntity s WHERE s.currentStreak > 0")
    Double findAverageActiveStreak();

    @Query("SELECT COUNT(s) FROM StreakEntity s WHERE s.currentStreak >= :days")
    Long countUsersWithStreakAtLeast(@Param("days") Integer days);

    boolean existsByUser(UserEntity user);
}
