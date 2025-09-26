package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, Long> {
    
    List<AchievementEntity> findByIsActiveTrue();
    
    @Query("SELECT a FROM AchievementEntity a WHERE a.pointsRequired <= :userPoints AND a.isActive = true")
    List<AchievementEntity> findEligibleAchievementsByPoints(@Param("userPoints") Integer userPoints);
    
    List<AchievementEntity> findAllByOrderByPointsRequiredAsc();
    
    @Query("SELECT a FROM AchievementEntity a WHERE a.title LIKE %:title%")
    List<AchievementEntity> findByTitleContaining(@Param("title") String title);
    
    boolean existsByTitle(String title);
}
