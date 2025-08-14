/*
 * @ {#} AuthService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.SignUpRequest;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public interface AuthService {
    /**
     * Handles user sign-up.
     *
     * @param signUpRequest the sign-up request containing user details
     * @return a confirmation message signup success or an error message
     */
    boolean signUpForCustomer(SignUpRequest signUpRequest);
}
