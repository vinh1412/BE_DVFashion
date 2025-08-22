/*
 * @ {#} CategoryService.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;


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
     * @param request the request containing category details
     * @param imageFile       the image file for the category
     * @return the created CategoryResponse
     */
    CategoryResponse createCategory(CategoryRequest request, MultipartFile imageFile);

    /**
     * Checks if a category with the given name exists, ignoring case.
     *
     * @param name the name of the category to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return the CategoryResponse if found, not found exception if not found
     */
     CategoryResponse getCategoryById(Long id);

    /**
     * Updates an existing category with the provided request and image file.
     * @param categoryRequest the request containing updated category details
     * @param id the ID of the category to update
     * @param imageFile the image file for the category, can be null if no image is provided
     * @return the updated CategoryResponse
     */
     CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id, MultipartFile imageFile);

    /**
     * Deactivates a category by its ID.
     *
     * @param id the ID of the category to deactivate
     */
    void deactivateCategory(Long id);

    /**
     * Retrieves a paginated list of categories.
     *
     * @param pageable the pagination information
     * @return a PageResponse containing the paginated list of CategoryResponse
     */
    PageResponse<CategoryResponse> getCategoriesPaging(Pageable pageable);
}
