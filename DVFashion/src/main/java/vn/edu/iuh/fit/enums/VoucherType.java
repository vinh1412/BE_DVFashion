/*
 * @ {#} VoucherType.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enum representing types of vouchers
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public enum VoucherType {
    SHOP_WIDE("SHOP_WIDE"),      // Applies to the entire shop
    PRODUCT_SPECIFIC("PRODUCT_SPECIFIC"); // Applies to specific products

    private final String type;

    VoucherType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
