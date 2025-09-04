/*
 * @ {#} NotFoundEnumValueException.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception for handling not found enum values
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public class NotFoundEnumValueException extends RuntimeException {
    public NotFoundEnumValueException(String message) {
        super(message);
    }
}
