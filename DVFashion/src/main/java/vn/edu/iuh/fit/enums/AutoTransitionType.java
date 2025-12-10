/*
 * @ {#} AutoTransitionType.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

import lombok.Getter;

/*
 * @description: Enumeration for automatic order transition types
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Getter
public enum AutoTransitionType {
    CONFIRMED_TO_PROCESSING("Auto transition to processing"),
    PROCESSING_TO_SHIPPED("Auto transition to shipped"),
    SHIPPED_TO_DELIVERED("Auto transition to delivered"),
    PENDING_TO_CANCELLED("Auto cancel unpaid orders"),
    DELIVERED_TO_COMPLETED("Auto complete delivered orders");

    private final String description;

    AutoTransitionType(String description) {
        this.description = description;
    }
}
