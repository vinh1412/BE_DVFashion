/*
 * @ {#} ProductReviewsResponse.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.util.List;

/*
 * @description: DTO class for product reviews response containing a list of reviews and their statistics.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
public record ProductReviewsResponse(
        List<ReviewResponse> reviews,

        ProductReviewStatistics statistics
) {
}
