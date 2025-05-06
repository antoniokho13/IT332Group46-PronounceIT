package com.capstone.group46.pronounceit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.capstone.group46.pronounceit.entity.CategoryEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.repository.CategoryRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CategoryEntity> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public CategoryEntity createCategory(CategoryEntity category) {
        // Fetch the UserEntity from the database
        Long userId = category.getCreatedBy().getId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        // Set the managed UserEntity to the CategoryEntity
        category.setCreatedBy(user);

        // Save the CategoryEntity
        return categoryRepository.save(category);
    }

    public Optional<CategoryEntity> updateCategory(Long categoryId, CategoryEntity updatedCategory) {
        return categoryRepository.findById(categoryId).map(category -> {
            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            category.setActive(updatedCategory.isActive());
            return categoryRepository.save(category);
        });
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}