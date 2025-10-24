/*
 * @ {#} UserInteractionService.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.enums.InteractionType;

import java.math.BigDecimal;

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
}
