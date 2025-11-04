/*
 * @ {#} ReviewReplyStatus.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enum representing the status of a review reply
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
public enum ReviewReplyStatus {
    APPROVED("Visible to everyone"),
    HIDDEN("Hidden from customers, visible to admin"),
    PENDING("Waiting for moderation");

    private final String description;

    ReviewReplyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
