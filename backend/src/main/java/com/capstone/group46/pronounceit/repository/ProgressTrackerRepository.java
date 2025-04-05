package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.ProgressTrackerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressTrackerRepository extends JpaRepository<ProgressTrackerEntity, Long> {
}