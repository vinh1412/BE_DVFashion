/*
 * @ {#} RecommendationModelVersionResponse.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for recommendation model version
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
public record RecommendationModelVersionResponse(
        Long id,

        String modelName,

        Double contentWeight,

        Double collaborativeWeight,

        Double precisionAt10,

        Double recallAt10,

        Double mapAt10,

        Boolean isActive,

        LocalDateTime createdAt
) {
}
