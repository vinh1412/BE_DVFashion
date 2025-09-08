/*
 * @ {#} PromotionService.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.PromotionRequest;
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
    /*
     * Creates a new promotion based on the provided request and language.
     *
     * @param promotionRequest The request object containing promotion details.
     * @param inputLang The language for the promotion.
     * @return The created PromotionResponse object.
     */
    PromotionResponse createPromotion(PromotionRequest promotionRequest, Language inputLang);

    /*
     * Retrieves a promotion by its ID and language.
     *
     * @param id The ID of the promotion to retrieve.
     * @param language The language for the promotion details.
     * @return The PromotionResponse object corresponding to the given ID and language.
     */
    PromotionResponse getPromotionById(Long id, Language language);

    /*
     * Updates an existing promotion identified by its ID with the provided request and language.
     *
     * @param promotionRequest The request object containing updated promotion details.
     * @param id The ID of the promotion to update.
     * @param language The language for the promotion.
     * @return The updated PromotionResponse object.
     */
    PromotionResponse updatePromotion(PromotionRequest promotionRequest, Long id, Language inputLang);

    /*
     * Retrieves all promotions in the specified language.
     *
     * @param language The language for the promotion details.
     * @return A list of PromotionResponse objects in the specified language.
     */
    List<PromotionResponse> getAllPromotions(Language language);
}
