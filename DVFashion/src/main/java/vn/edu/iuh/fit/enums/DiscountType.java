/*
 * @ {#} DiscountType.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enum representing types of discounts
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public enum DiscountType {
    PERCENTAGE("PERCENTAGE"),    // Discount by percentage
    FIXED_AMOUNT("FIXED_AMOUNT");   // Discount by fixed amount

    private final String type;

    DiscountType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
