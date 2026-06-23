package com.tasktracker.service;

import com.tasktracker.entity.Category;
import com.tasktracker.entity.User;
import com.tasktracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getUserCategories(User user) {
        return categoryRepository.findByUserOrderByNameAsc(user);
    }

    @Transactional
    public Category createCategory(String name, User user) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        String trimmed = name.trim();

        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Category name must be under 100 characters.");
        }

        if (categoryRepository.existsByNameAndUser(trimmed, user)) {
            throw new IllegalArgumentException("A category with that name already exists.");
        }
        return categoryRepository.save(Category.builder().name(trimmed).user(user).build());
    }

    @Transactional
    public void deleteCategory(Long id, User user) {
        Category category = categoryRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        categoryRepository.delete(category);
    }
}
