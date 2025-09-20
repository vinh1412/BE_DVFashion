/*
 * @ {#} CartLimitExceededException.java   1.0     19/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Exception thrown when cart item limit is exceeded
 * @author: Tran Hien Vinh
 * @date:   19/09/2025
 * @version:    1.0
 */
public class CartLimitExceededException extends RuntimeException {
    public CartLimitExceededException(String message) {
        super(message);
    }
}
