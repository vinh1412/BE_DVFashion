/*
 * @ {#} InsufficientStockException.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Exception thrown when there is insufficient stock for a product variant
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
