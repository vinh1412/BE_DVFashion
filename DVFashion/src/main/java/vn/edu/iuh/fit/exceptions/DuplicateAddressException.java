/*
 * @ {#} DuplicateAddressException.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception thrown when attempting to create a duplicate address for a user.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
public class DuplicateAddressException extends RuntimeException {
    public DuplicateAddressException(String message) {
        super(message);
    }
}
