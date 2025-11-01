/*
 * @ {#} ResourceNotFoundException.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description: Custom exception thrown when a requested resource is not found
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
