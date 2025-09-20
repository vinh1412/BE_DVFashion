/*
 * @ {#} ProductRecommendationResponse.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/*
 * @description: DTO for Product Recommendation response
 * @author: Tran Hien Vinh
 * @date:   20/09/2025
 * @version:    1.0
 */

public record ProductRecommendationResponse(
        @JsonProperty("product_id")
        Long productId,

        @JsonProperty("similarity_score")
        Double similarityScore,

        String name,

        String category,

        String brand,

        BigDecimal price
) {
}
