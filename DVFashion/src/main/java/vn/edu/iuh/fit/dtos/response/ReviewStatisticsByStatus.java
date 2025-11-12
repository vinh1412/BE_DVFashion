/*
 * @ {#} ReviewStatisticsByStatus.java   1.0     17/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO class for review statistics grouped by their status.
 * @author: Tran Hien Vinh
 * @date:   17/10/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.enums.ReviewStatus;
import java.util.Map;

public record ReviewStatisticsByStatus(
        long totalReviews,

        Double averageRating,

        Map<ReviewStatus, Long> statusCounts
) {}
