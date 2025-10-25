/*
 * @ {#} UserInteractionService.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.UserProductInteractionResponse;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @description: Service interface for tracking user interactions with products
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
public interface UserInteractionService {
    /**
     * Tracks a user interaction with a product.
     *
     * @param userId          The ID of the user.
     * @param productId       The ID of the product.
     * @param interactionType The type of interaction (e.g., VIEW, CLICK, PURCHASE, RATING).
     * @param rating          The rating given by the user (if applicable).
     */
    void trackInteraction(Long userId, Long productId, InteractionType interactionType, BigDecimal rating);

    /**
     * Retrieves a paginated list of user-product interactions based on provided filters.
     *
     * @param userId          (Optional) The ID of the user.
     * @param productId       (Optional) The ID of the product.
     * @param interactionType (Optional) The type of interaction.
     * @param fromDate        (Optional) The start date for filtering interactions.
     * @param toDate          (Optional) The end date for filtering interactions.
     * @param page            The page number to retrieve.
     * @param size            The number of records per page.
     * @return A paginated response containing user-product interactions matching the filters.
     */
    PageResponse<UserProductInteractionResponse> findAllWithFilters(
            Long userId,
            Long productId,
            InteractionType interactionType,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);
}
