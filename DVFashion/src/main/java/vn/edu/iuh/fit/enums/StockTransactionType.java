/*
 * @ {#} StockTransactionType.java   1.0     9/6/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enum for stock transaction types
 * @author: Nguyen Tan Thai Duong
 * @date:   9/6/2025
 * @version:    1.0
 */
public enum StockTransactionType {
    INBOUND("INBOUND"),
    OUTBOUND("OUTBOUND"),
    RESERVE("RESERVE"),
    RELEASE("RELEASE"),
    CONFIRMED("CONFIRMED"),
    ADJUSTMENT_IN("ADJUSTMENT_IN"),
    ADJUSTMENT_OUT("ADJUSTMENT_OUT"),
    RETURN("RETURN");

    private final String description;

    StockTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
