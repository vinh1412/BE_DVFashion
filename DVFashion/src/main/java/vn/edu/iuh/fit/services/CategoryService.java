/*
 * @ {#} CategoryService.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;


/*
 * @description: Service interface for managing categories
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
public interface CategoryService {
    /**
     * Creates a new category with the provided request and image file.
     *
     * @param categoryRequest the request containing category details
     * @param imageFile       the image file for the category
     * @return the created CategoryResponse
     */
    CategoryResponse createCategory(CategoryRequest categoryRequest, MultipartFile imageFile);

    /**
     * Checks if a category with the given name exists, ignoring case.
     *
     * @param name the name of the category to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}
