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
import vn.edu.iuh.fit.enums.Language;

import java.util.List;


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
     * @param imageFile the image file for the category
     * @param inputLang the language of the input data
     * @return the created CategoryResponse
     */
    CategoryResponse createCategory(CategoryRequest request, MultipartFile imageFile, Language inputLang);

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @param language the language for the category data
     * @return the CategoryResponse if found, not found exception if not found
     */
     CategoryResponse getCategoryById(Long id, Language language);

    /**
     * Updates an existing category with the provided request and image file.
     * @param categoryRequest the request containing updated category details
     * @param id the ID of the category to update
     * @param imageFile the image file for the category, can be null if no image is provided
     * @param language the language of the input data
     * @return the updated CategoryResponse
     */
     CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id, MultipartFile imageFile, Language language);

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
     * @param language the language for the category data
     * @return a PageResponse containing the paginated list of CategoryResponse
     */
    PageResponse<CategoryResponse> getCategoriesPaging(Pageable pageable, Language language);

    /**
     * Retrieves all categories.
     *
     * @param language the language for the category data
     * @return a list of all CategoryResponse
     */
    List<CategoryResponse> getAllCategories(Language language);
}
