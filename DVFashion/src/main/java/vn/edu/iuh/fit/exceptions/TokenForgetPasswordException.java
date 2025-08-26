/*
 * @ {#} TokenRefreshException.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception to handle errors related to token forget password operations
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
public class TokenForgetPasswordException extends RuntimeException {

    public TokenForgetPasswordException(String message) {
        super(message);
    }
}
