package com.cozystay.service;

import com.cozystay.dto.category.CategoryRequest;
import com.cozystay.dto.category.CategoryResponse;
import com.cozystay.exception.ResourceAlreadyExistsException;
import com.cozystay.exception.ResourceNotFoundException;
import com.cozystay.exception.UnauthorizedException;
import com.cozystay.model.Category;
import com.cozystay.model.User;
import com.cozystay.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getAllCategoriesAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can view all categories including inactive ones
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to view all categories");
        }

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can create categories
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to create categories");
        }

        // Check if category with same name already exists
        if (categoryRepository.existsByNameIgnoreCase(categoryRequest.getName())) {
            throw new ResourceAlreadyExistsException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .iconUrl(categoryRequest.getIconUrl())
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can update categories
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to update categories");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if name is being changed and if it already exists
        if (categoryRequest.getName() != null &&
                !category.getName().equalsIgnoreCase(categoryRequest.getName()) &&
                categoryRepository.existsByNameIgnoreCase(categoryRequest.getName())) {
            throw new ResourceAlreadyExistsException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        if (categoryRequest.getName() != null) {
            category.setName(categoryRequest.getName());
        }

        if (categoryRequest.getDescription() != null) {
            category.setDescription(categoryRequest.getDescription());
        }

        if (categoryRequest.getIconUrl() != null) {
            category.setIconUrl(categoryRequest.getIconUrl());
        }

        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }

    @Transactional
    public CategoryResponse toggleCategoryActiveStatus(Long id, boolean isActive) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can toggle category status
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to toggle category status");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(isActive);
        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only admins can delete categories
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("You don't have permission to delete categories");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Instead of deleting, we can mark as inactive if it has related services
        if (!category.getServices().isEmpty()) {
            category.setActive(false);
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }
    }
}