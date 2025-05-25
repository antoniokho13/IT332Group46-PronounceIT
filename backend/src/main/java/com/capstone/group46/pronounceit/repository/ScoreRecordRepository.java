package com.capstone.group46.pronounceit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capstone.group46.pronounceit.entity.ScoreRecordEntity;

@Repository
public interface ScoreRecordRepository extends JpaRepository<ScoreRecordEntity, Long> {
    Optional<ScoreRecordEntity> findByUser_IdAndLesson_LessonIdAndSessionId(Long userId, Long lessonId, String sessionId);

    List<ScoreRecordEntity> findTop1ByUser_IdAndLesson_LessonIdOrderByCompletionDateDesc(Long userId, Long lessonId);
}