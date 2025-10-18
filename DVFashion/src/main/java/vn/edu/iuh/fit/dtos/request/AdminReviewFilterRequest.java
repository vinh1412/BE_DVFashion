/*
 * @ {#} AdminReviewFilterRequest.java   1.0     17/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import vn.edu.iuh.fit.enums.ReviewStatus;

/*
 * @description: DTO class for filtering reviews in the admin panel.
 * @author: Tran Hien Vinh
 * @date:   17/10/2025
 * @version:    1.0
 */
public record AdminReviewFilterRequest(
        ReviewStatus status,

        String sortBy, // createdAt, rating, status

        String sortDirection // ASC, DESC
) {
    public AdminReviewFilterRequest {
        // Default values
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "DESC";
        }

        // Validate sortBy
        if (!sortBy.equals("createdAt") && !sortBy.equals("rating") && !sortBy.equals("status")) {
            throw new IllegalArgumentException("Invalid sortBy field. Allowed values: createdAt, rating, status");
        }

        // Validate sortDirection
        if (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Invalid sortDirection. Allowed values: ASC, DESC");
        }
    }
}
