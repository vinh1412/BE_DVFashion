/*
 * @ {#} AdminReviewsResponse.java   1.0     17/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.util.List;

/*
 * @description: DTO class for admin reviews response containing a list of reviews and their statistics.
 * @author: Tran Hien Vinh
 * @date:   17/10/2025
 * @version:    1.0
 */
public record AdminReviewsResponse(
        List<ReviewResponse> reviews,

        ReviewStatisticsByStatus statistics
) {}
