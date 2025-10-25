/*
 * @ {#} RecommendationConfigResponse.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

/*
 * @description: DTO for recommendation configuration response
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Builder
public record RecommendationConfigResponse (
     String key,

     String value,

     String description
){}
