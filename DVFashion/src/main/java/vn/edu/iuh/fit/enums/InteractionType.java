/*
 * @ {#} InteractionType.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.enums;
/*
 * @description: Enum representing types of user-product interactions
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
public enum InteractionType {
    VIEW("View"),
    ADD_TO_CART("Add to Cart"),
    PURCHASE("Purchase"),
    REVIEW("Review");

    private final String value;

    InteractionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
