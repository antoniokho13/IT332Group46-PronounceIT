package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.ScoreRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, Long> {
}