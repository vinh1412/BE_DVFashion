/*
 * @ {#} UpdateReviewReplyRequest.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;

/*
 * @description: Request DTO for updating a review reply.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public record UpdateReviewReplyRequest(
        @NotBlank(message = "Content is required")
        String content
) {}
