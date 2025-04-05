package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.CategoryEntity;
import com.capstone.group46.pronounceit.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CategoryEntity> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public CategoryEntity createCategory(CategoryEntity category) {
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