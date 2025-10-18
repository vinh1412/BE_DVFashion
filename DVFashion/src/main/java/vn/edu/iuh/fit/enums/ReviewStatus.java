/*
 * @ {#} ReviewStatus.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enum representing the status of a product review.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public enum ReviewStatus {
    PENDING("Pending"), // Pending approval
    AUTO_APPROVED("Auto Approved"), // Automatic approval via AI/filter
    APPROVED("Approved"), // Manual approval, public display
    NEED_REVIEW("Need Review"), // Marked suspicious by AI, need admin to check
    REJECTED("Rejected"), // Rejected by admin, not displayed
    HIDDEN("Hidden"); // Hidden by user/admin, not displayed still saved in DB, not public

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }
}
