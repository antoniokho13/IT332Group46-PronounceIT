package com.capstone.group46.pronounceit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capstone.group46.pronounceit.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    
    // Methods for accumulated points queries (optional - can be added later if needed)
    // List<UserEntity> findByAccumulatedPointsGreaterThanEqual(Integer points);
    // List<UserEntity> findByAccumulatedPointsBetween(Integer minPoints, Integer maxPoints);
    // List<UserEntity> findAllByOrderByAccumulatedPointsDesc();
}