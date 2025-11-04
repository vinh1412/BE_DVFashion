/*
 * @ {#} RecommendationService.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.ProductRecommendationStatsResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.dtos.response.RecommendationAnalyticsResponse;
import vn.edu.iuh.fit.dtos.response.TopRecommendedProductResponse;

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

    /**
     * Get hybrid product recommendations based on user ID and/or product ID.
     *
     * @param userId             The ID of the user (optional).
     * @param productId          The ID of the product (optional).
     * @param numRecommendations The number of recommendations to retrieve.
     * @return A list of recommended products.
     */
    List<ProductResponse> getHybridRecommendations(Long userId, Long productId, int numRecommendations);

    /**
     * Get the top recommended products within a specified time frame.
     *
     * @param limit The maximum number of top products to retrieve.
     * @param days  The number of days to look back for recommendations (optional).
     * @return A list of top recommended products.
     */
    List<TopRecommendedProductResponse> getTopRecommendedProducts(int limit, Integer days);

    /**
     * Get analytics data for recommendations within a specified time frame.
     *
     * @param days The number of days to look back for analytics (optional).
     * @return Recommendation analytics data.
     */
    RecommendationAnalyticsResponse getRecommendationAnalytics(Integer days);

    /**
     * Get product recommendation statistics within a specified time frame.
     *
     * @param limit The maximum number of products to retrieve statistics for.
     * @param days  The number of days to look back for statistics (optional).
     * @return A list of product recommendation statistics.
     */
    List<ProductRecommendationStatsResponse> getProductRecommendationStats(int limit, Integer days);
}
