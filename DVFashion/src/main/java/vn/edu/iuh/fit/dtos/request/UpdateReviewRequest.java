/*
 * @ {#} UpdateReviewRequest.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/*
 * @description: DTO class for updating an existing product review request.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public record UpdateReviewRequest(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment,

        List<String> imagesToDelete
) {}
