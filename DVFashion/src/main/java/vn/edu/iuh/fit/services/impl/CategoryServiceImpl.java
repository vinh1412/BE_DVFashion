/*
 * @ {#} CategoryServiceImpl.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.CategoryMapper;
import vn.edu.iuh.fit.repositories.CategoryRepository;
import vn.edu.iuh.fit.services.CategoryService;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.utils.ImageUtils;

/*
 * @description: Service implementation for managing categories
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    private  final CloudinaryService cloudinaryService;

    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest, MultipartFile imageFile) {
        // Check if the category with the same name already exists
        if(existsByNameIgnoreCase(categoryRequest.name().toLowerCase())) {
            throw new AlreadyExistsException("Category with name '" + categoryRequest.name() + "' already exists.");
        }

        // Create the image URL using the Cloudinary service
        String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);

        // Map the request DTO to the entity
        Category category = categoryMapper.toEntity(categoryRequest);
        category.setImage(imageUrl);

        Category savedCategory = categoryRepository.save(category);

        // Map the saved entity back to the response DTO
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id, MultipartFile imageFile) {
        // Retrieve the existing category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Check if the category with the same name already exists, excluding the current category
        if (categoryRequest.name() != null
                && !categoryRequest.name().equalsIgnoreCase(category.getName())
                && existsByNameIgnoreCase(categoryRequest.name().toLowerCase())) {
            throw new AlreadyExistsException("Category with name '" + categoryRequest.name() + "' already exists.");
        }

        // Update the category fields
        categoryMapper.updateEntityFromDto(categoryRequest, category);

        // If an image file is provided, update the image URL
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);
            category.setImage(imageUrl);
        }

        // Save the updated category
        Category updatedCategory = categoryRepository.save(category);

        // Map the updated entity back to the response DTO
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deactivateCategory(Long id) {
        // Retrieve the existing category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Set the category as inactive
        category.setActive(false);

        // Save the updated category
        categoryRepository.save(category);
    }

    @Override
    public PageResponse<CategoryResponse> getCategoriesPaging(Pageable pageable) {
        // Retrieve all categories with pagination
        Page<Category> categories = categoryRepository.findAll(pageable);

        // Map the Page<Category> to Page<CategoryResponse> using the categoryMapper
        Page<CategoryResponse> dtoPage = categories.map(categoryMapper::toDto);

        // Convert the Page<CategoryResponse> to PageResponse<CategoryResponse>
        return PageResponse.from(dtoPage);
    }
}
