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
import vn.edu.iuh.fit.entities.Promotion;
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
    PromotionResponse createPromotion(CreatePromotionRequest createPromotionRequest, Language inputLang, MultipartFile bannerFile);

    /*
     * Updates an existing promotion identified by its ID with the provided request and language.
     *
     * @param updatePromotionRequest The request object containing updated promotion details.
     * @param id The ID of the promotion to update.
     * @param inputLang The language for the promotion.
     * @return The updated PromotionResponse object.
     */
    PromotionResponse updatePromotion(UpdatePromotionRequest updatePromotionRequest, Long id, Language inputLang, MultipartFile bannerFile);

    PromotionResponse getPromotionById(Long id, Language language);

    List<PromotionResponse> getAllPromotions(Language language);

    PageResponse<PromotionResponse> getPromotionsPaging(Pageable pageable, Language language);
    //    /*
//     * Retrieves a promotion by its ID and language.
//     *
//     * @param id The ID of the promotion to retrieve.
//     * @param language The language for the promotion details.
//     * @return The PromotionResponse object corresponding to the given ID and language.
//     */
//    PromotionResponse getPromotionById(Long id, Language language);
//
//    /*
//     * Updates an existing promotion identified by its ID with the provided request and language.
//     *
//     * @param promotionRequest The request object containing updated promotion details.
//     * @param id The ID of the promotion to update.
//     * @param language The language for the promotion.
//     * @return The updated PromotionResponse object.
//     */
//    PromotionResponse updatePromotion(CreatePromotionRequest createPromotionRequest, Long id, Language inputLang);
//
//    /*
//     * Retrieves all promotions in the specified language.
//     *
//     * @param language The language for the promotion details.
//     * @return A list of PromotionResponse objects in the specified language.
//     */
//    List<PromotionResponse> getAllPromotions(Language language);
//
//    /*
//     * Validates a promotion by its ID.
//     *
//     * @param promotionId The ID of the promotion to validate.
//     * @return The validated Promotion entity.
//     */
//    Promotion validatePromotion(Long promotionId);
}
