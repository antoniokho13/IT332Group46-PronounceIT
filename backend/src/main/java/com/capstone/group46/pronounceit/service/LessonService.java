package com.capstone.group46.pronounceit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.capstone.group46.pronounceit.entity.CategoryEntity;
import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.repository.CategoryRepository;
import com.capstone.group46.pronounceit.repository.LessonRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public LessonService(LessonRepository lessonRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<LessonEntity> getAllLessons() {
        return lessonRepository.findAll();
    }

    public Optional<LessonEntity> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    public LessonEntity createLesson(LessonEntity lesson) {
        // Fetch the CategoryEntity from the database
        Long categoryId = lesson.getCategory().getCategoryId();
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + categoryId + " not found"));

        // Fetch the UserEntity from the database
        Long userId = lesson.getCreatedBy().getId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        // Set the managed entities
        lesson.setCategory(category);
        lesson.setCreatedBy(user);

        // Save the LessonEntity
        return lessonRepository.save(lesson);
    }

    public Optional<LessonEntity> updateLesson(Long lessonId, LessonEntity updatedLesson) {
        return lessonRepository.findById(lessonId).map(lesson -> {
            lesson.setName(updatedLesson.getName());
            lesson.setFocus(updatedLesson.getFocus());
            lesson.setSequence(updatedLesson.getSequence());
            lesson.setActive(updatedLesson.isActive());
            return lessonRepository.save(lesson);
        });
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
}