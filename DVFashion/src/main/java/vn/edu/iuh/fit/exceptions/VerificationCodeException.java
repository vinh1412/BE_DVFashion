/*
 * @ {#} VerificationCodeException.java   1.0     02/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception for handling verification code errors
 * @author: Tran Hien Vinh
 * @date:   02/09/2025
 * @version:    1.0
 */
public class VerificationCodeException extends RuntimeException {
    public VerificationCodeException(String message) {
        super(message);
    }
}
