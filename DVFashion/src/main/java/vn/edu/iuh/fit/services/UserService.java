/*
 * @ {#} UserService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.entities.User;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public interface UserService {
    /**
     * Check if a user exists by their email.
     *
     * @param email the email to check
     * @return true if a user with the specified email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists by their phone number.
     *
     * @param phone the phone number to check
     * @return true if a user with the specified phone number exists, false otherwise
     */
    boolean existsByPhone(String phone);

    /**
     * Create a new customer.
     *
     * @param signUpRequest the request containing user details for sign up
     * @return the created User entity
     */
    User createCustomer(SignUpRequest signUpRequest);
}
