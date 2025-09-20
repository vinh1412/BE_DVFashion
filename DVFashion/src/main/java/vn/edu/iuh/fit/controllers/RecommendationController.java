/*
 * @ {#} RecommendationController.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
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

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductRecommendations(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<ProductResponse> recommendations = recommendationService.getRecommendations(productId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommendations retrieved successfully"));
    }
}
