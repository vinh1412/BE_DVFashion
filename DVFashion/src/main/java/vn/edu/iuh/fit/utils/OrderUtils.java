/*
 * @ {#} OrderUtils.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/*
 * @description: Utility class for generating order numbers and transaction IDs
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@UtilityClass
public class OrderUtils {

    public String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String generateReserveReference(String orderNumber, Long cartItemId) {
        return String.format(
                "ORD_%s_ITEM_%d_%s",
                orderNumber,
                cartItemId,
                UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
