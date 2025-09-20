/*
 * @ {#} RecommendationRequest.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * @description: DTO for recommendation request data
 * @author: Tran Hien Vinh
 * @date:   20/09/2025
 * @version:    1.0
 */
public record RecommendationRequest(
        @JsonProperty("product_id")
        Long productId,

        @JsonProperty("num_recommendations")
        Integer num_recommendations
) {}
