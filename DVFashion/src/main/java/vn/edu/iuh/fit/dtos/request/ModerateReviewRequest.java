/*
 * @ {#} ModerateReviewRequest.java   1.0     17/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * @description: DTO class for admin to moderate product reviews.
 * @author: Tran Hien Vinh
 * @date:   17/10/2025
 * @version:    1.0
 */
public record ModerateReviewRequest(
        @NotBlank(message = "New status cannot be null")
        @Pattern(regexp = "PENDING|AUTO_APPROVED|APPROVED|NEED_REVIEW|REJECTED|HIDDEN",
                 message = "Invalid status. Allowed values: PENDING, AUTO_APPROVED, APPROVED, NEED_REVIEW, REJECTED, HIDDEN")
        String newStatus,

        String adminComment
) {}
