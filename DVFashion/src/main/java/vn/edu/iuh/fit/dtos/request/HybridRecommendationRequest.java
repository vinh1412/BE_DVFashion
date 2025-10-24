/*
 * @ {#} HybridRecommendationRequest.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/*
 * @description: DTO for hybrid recommendation request
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Builder
public record HybridRecommendationRequest(
    @JsonProperty("user_id")
    Long userId,

    @JsonProperty("product_id")
    Long productId,

    @JsonProperty("num_recommendations")
    int numRecommendations,

    @JsonProperty("use_content_based")
    boolean useCollaborative
){}
