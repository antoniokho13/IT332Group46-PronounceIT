package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PronounciationAttemptRepository extends JpaRepository<PronounciationAttemptEntity, Long> {
}