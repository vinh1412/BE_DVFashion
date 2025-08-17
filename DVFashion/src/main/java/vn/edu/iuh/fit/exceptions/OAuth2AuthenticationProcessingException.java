/*
 * @ {#} OAuth2AuthenticationProcessingException.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

import org.springframework.security.core.AuthenticationException;

/*
 * @description: Exception thrown when there is an error during OAuth2 authentication processing
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    public OAuth2AuthenticationProcessingException(String message) {
        super(message);
    }
}
