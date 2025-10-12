/*
 * @ {#} PaymentException.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception for payment processing errors
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
