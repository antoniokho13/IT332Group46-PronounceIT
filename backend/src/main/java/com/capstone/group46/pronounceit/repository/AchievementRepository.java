package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.AchievementEntity.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, Long> {
    
    List<AchievementEntity> findByIsActiveTrue();
    
    List<AchievementEntity> findByTriggerType(TriggerType triggerType);
    
    List<AchievementEntity> findByTriggerTypeAndIsActiveTrue(TriggerType triggerType);
    
    @Query("SELECT a FROM AchievementEntity a WHERE a.triggerType = :triggerType AND a.triggerValue <= :value AND a.isActive = true")
    List<AchievementEntity> findEligibleAchievements(@Param("triggerType") TriggerType triggerType, @Param("value") Integer value);
    
    @Query("SELECT a FROM AchievementEntity a WHERE a.title LIKE %:title%")
    List<AchievementEntity> findByTitleContaining(@Param("title") String title);
    
    boolean existsByTitle(String title);
}
