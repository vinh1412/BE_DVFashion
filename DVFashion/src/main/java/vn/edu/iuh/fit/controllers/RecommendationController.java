/*
 * @ {#} RecommendationController.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.services.RecommendationService;

import java.util.List;

/*
 * @description: Controller for handling product recommendation requests
 * @author: Tran Hien Vinh
 * @date:   20/09/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getHybridRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "5") int limit) {


        List<ProductResponse> recommendations = recommendationService
                .getHybridRecommendations(userId, productId, limit);

        return ResponseEntity.ok(
                ApiResponse.success(recommendations, "Hybrid recommendations retrieved successfully")
        );
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/stats/top-products")
    public ResponseEntity<ApiResponse<List<TopRecommendedProductResponse>>> getTopRecommendedProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer days
    ) {
        List<TopRecommendedProductResponse> topProducts = recommendationService.getTopRecommendedProducts(limit, days);
        return ResponseEntity.ok(
                ApiResponse.success(topProducts, "Top recommended products retrieved successfully")
        );
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/stats/analytics")
    public ResponseEntity<ApiResponse<RecommendationAnalyticsResponse>> getRecommendationAnalytics(
            @RequestParam(required = false) Integer days
    ) {
        RecommendationAnalyticsResponse analytics = recommendationService.getRecommendationAnalytics(days);
        return ResponseEntity.ok(
                ApiResponse.success(analytics, "Recommendation analytics retrieved successfully")
        );
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/stats/products")
    public ResponseEntity<ApiResponse<List<ProductRecommendationStatsResponse>>> getProductRecommendationStats(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer days
    ) {
        List<ProductRecommendationStatsResponse> stats =
                recommendationService.getProductRecommendationStats(limit, days);
        return ResponseEntity.ok(
                ApiResponse.success(stats, "Product recommendation statistics retrieved successfully")
        );
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTodayRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<ProductResponse> recommendations = recommendationService.getTodayRecommendations(userId, limit);

        String message = userId != null
                ? "Personalized today's recommendations retrieved successfully"
                : "Popular products retrieved successfully";

        return ResponseEntity.ok(ApiResponse.success(recommendations, message));
    }
}
