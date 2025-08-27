/*
 * @ {#} ProductVariantStatus.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public enum ProductVariantStatus {
    ACTIVE("ACTIVE"),

    INACTIVE("INACTIVE"),

    OUT_OF_STOCK("OUT OF STOCK");

    private final String value;

    ProductVariantStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
