/*
 * @ {#} OrderException.java   1.0     12/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception class for order-related errors
 * @author: Tran Hien Vinh
 * @date:   12/10/2025
 * @version:    1.0
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}
