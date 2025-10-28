/*
 * @ {#} PhoneAlreadyExistsException.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception to indicate that an entity is not active.
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public class NotActiveException extends RuntimeException {
    public NotActiveException(String message) {
        super(message);
    }
}
