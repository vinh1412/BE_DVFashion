/*
 * @ {#} ModerateReviewReplyRequest.java   1.0     04/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/*
 * @description: Request DTO for moderating review replies.
 * @author: Tran Hien Vinh
 * @date:   04/11/2025
 * @version:    1.0
 */
public record ModerateReviewReplyRequest(
        @NotNull(message = "New status is required")
        @Pattern(regexp = "APPROVED|PENDING|HIDDEN", message = "Status must be APPROVED, PENDING, HIDDEN, or REJECTED")
        String newStatus
) {}
