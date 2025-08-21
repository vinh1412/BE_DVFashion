/*
 * @ {#} CategoryServiceImpl.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
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

    @Override
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile imageFile) {
        // Check if the category with the same name already exists
        if(existsByNameIgnoreCase(request.getName().toLowerCase())) {
            throw new AlreadyExistsException("Category with name '" + request.getName() + "' already exists.");
        }

        // Create the image URL using the Cloudinary service
        String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .image(imageUrl)
                .active(request.isActive())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getDescription(),
                savedCategory.getImage(),
                savedCategory.isActive()
        );
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
