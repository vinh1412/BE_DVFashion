/*
 * @ {#} ContentModerationResult.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO class for content moderation result.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
public record ContentModerationResult(
        boolean isViolated,

        String reason,

        double confidenceScore
) {}
