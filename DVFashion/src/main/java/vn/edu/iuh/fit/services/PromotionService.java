/*
 * @ {#} PromotionService.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.PromotionRequest;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.enums.Language;

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
}
