/*
 * @ {#} RecommendationService.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.ProductResponse;

import java.util.List;

/*
 * @description: Service interface for managing product recommendations
 * @author: Tran Hien Vinh
 * @date:   20/09/2025
 * @version:    1.0
 */
public interface RecommendationService {
    /**
     * Get product recommendations based on a given product ID.
     *
     * @param productId          The ID of the product for which to get recommendations.
     * @param numRecommendations The number of recommendations to retrieve.
     * @return A list of recommended products.
     */
    List<ProductResponse> getRecommendations(Long productId, int numRecommendations);
}
