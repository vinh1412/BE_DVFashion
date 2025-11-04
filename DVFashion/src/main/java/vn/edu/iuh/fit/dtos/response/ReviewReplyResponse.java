/*
 * @ {#} ReviewReplyResponse.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.ReviewReplyStatus;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Response DTO for review replies.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public record ReviewReplyResponse(
        Long id,

        Long reviewId,

        Long parentReplyId,

        Long userId,

        String userName,

        String content,

        ReviewReplyStatus status,

        boolean edited,

        LocalDateTime createdAt,

        LocalDateTime editedAt,

        List<ReviewReplyResponse> childReplies
) {}
