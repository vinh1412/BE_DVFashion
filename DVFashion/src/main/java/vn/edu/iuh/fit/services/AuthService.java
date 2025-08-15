/*
 * @ {#} AuthService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.RefreshTokenRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;

/*
 * @description: Service interface for handling authentication operations
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

    /**
     * Handles user sign-in.
     *
     * @param signInRequest the sign-in request containing user credentials
     * @return a JwtResponse containing JWT tokens and user details
     */
    SignInResponse signIn(SignInRequest signInRequest);

    /**
     * Refreshes the JWT token.
     *
     * @param request the request containing the refresh token
     * @return a JwtResponse containing the new JWT tokens
     */
    SignInResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs out the user by invalidating the refresh token.
     *
     * @param refreshToken the refresh token to be invalidated
     */
    void logout(String refreshToken);
}
