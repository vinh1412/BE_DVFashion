/*
 * @ {#} BrandService.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.BrandRequest;
import vn.edu.iuh.fit.dtos.response.BrandResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.enums.Language;

/*
 * @description: Service interface for managing brands
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public interface BrandService {
    /**
     * Creates a new brand with the provided request and image file.
     *
     * @param request the request containing brand details
     * @param logoFile the image file for the brand logo
     * @param inputLang the language of the input data
     * @return the created BrandResponse
     */
    BrandResponse createBrand(BrandRequest request, MultipartFile logoFile, Language inputLang);

    /**
     * Retrieves a brand by its ID.
     *
     * @param id the ID of the brand to retrieve
     * @param language the language for the brand data
     * @return the BrandResponse if found, not found exception if not found
     */
    BrandResponse getBrandById(Long id, Language language);

    /**
     * Updates an existing brand with the provided request and image file.
     * @param brandRequest the request containing updated brand details
     * @param id the ID of the brand to update
     * @param logoFile the image file for the brand logo, can be null if no image is provided
     * @param language the language of the input data
     * @return the updated BrandResponse
     */
    BrandResponse updateBrand(BrandRequest brandRequest, Long id, MultipartFile logoFile, Language language);

    /**
     * Deactivates a brand by its ID.
     *
     * @param id the ID of the brand to deactivate
     */
    void deactivateBrand(Long id);

    /**
     * Retrieves a paginated list of brands.
     *
     * @param pageable the pagination information
     * @param language the language for the brand data
     * @return a PageResponse containing the paginated list of BrandResponse
     */
    PageResponse<BrandResponse> getBrandsPaging(Pageable pageable, Language language);
}
