/*
 * @ {#} PaypalException.java   1.0     13/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception class for handling PayPal-related errors.
 * @author: Tran Hien Vinh
 * @date:   13/10/2025
 * @version:    1.0
 */
public class PaypalException extends RuntimeException {
    public PaypalException(String message) {
        super(message);
    }
}
