/*
 * @ {#} CreateRecommendationModelVersionRequest.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description: Request DTO for creating a new recommendation model version
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
public record CreateRecommendationModelVersionRequest(
        @NotBlank(message = "Model name must not be blank")
        String modelName
) {}
