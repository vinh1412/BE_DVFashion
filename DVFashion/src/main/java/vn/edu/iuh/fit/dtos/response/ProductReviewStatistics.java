/*
 * @ {#} ProductReviewStatistics.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.util.Map;

/*
 * @description: DTO class for product review statistics.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
public record ProductReviewStatistics(
        long totalReviews,

        double averageRating,

        Map<Integer, Long> ratingCounts,

        long reviewsWithImages,

        long reviewsWithComments
) {
}
