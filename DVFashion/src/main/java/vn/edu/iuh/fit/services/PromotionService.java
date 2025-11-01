/*
 * @ {#} PromotionService.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CreatePromotionRequest;
import vn.edu.iuh.fit.dtos.request.UpdatePromotionRequest;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.enums.Language;

import java.util.List;

/*
 * @description: Service interface for managing promotions
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public interface PromotionService {
    /**
     * Creates a new promotion based on the provided request and language.
     *
     * @param createPromotionRequest The request object containing promotion details.
     * @param inputLang The language for the promotion.
     * @return The created PromotionResponse object.
     **/
    PromotionResponse createPromotion(CreatePromotionRequest createPromotionRequest, Language inputLang, MultipartFile bannerFile);

    /**
     * Updates an existing promotion identified by its ID with the provided request and language.
     *
     * @param updatePromotionRequest The request object containing updated promotion details.
     * @param id The ID of the promotion to update.
     * @param inputLang The language for the promotion.
     * @return The updated PromotionResponse object.
     **/
    PromotionResponse updatePromotion(UpdatePromotionRequest updatePromotionRequest, Long id, Language inputLang, MultipartFile bannerFile);

    /**
     * Retrieves a promotion by its ID in the specified language.
     *
     * @param id The ID of the promotion.
     * @param language The language for the promotion details.
     * @return The PromotionResponse object.
     */
    PromotionResponse getPromotionById(Long id, Language language);

    /**
     * Retrieves all promotions in the specified language.
     *
     * @param language The language for the promotion details.
     * @return A list of PromotionResponse objects.
     */
    List<PromotionResponse> getAllPromotions(Language language);

    /**
     * Retrieves a paginated list of promotions in the specified language.
     *
     * @param pageable The pagination information.
     * @param language The language for the promotion details.
     * @return A PageResponse containing PromotionResponse objects.
     */
    PageResponse<PromotionResponse> getPromotionsPaging(Pageable pageable, Language language);

    /**
     * Removes a product from a promotion.
     *
     * @param promotionId The ID of the promotion.
     * @param productId   The ID of the product to remove.
     */
    void removeProductFromPromotion(Long promotionId, Long productId);
}
