/*
 * @ {#} CreateReviewReplyRequest.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * @description: Request DTO for creating a review reply.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public record CreateReviewReplyRequest(
        @NotNull(message = "Review ID is required")
        Long reviewId,

        Long parentReplyId, // null if replying to review directly

        @NotBlank(message = "Content is required")
        String content
) {}
