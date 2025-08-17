/*
 * @ {#} AuthService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
     * @param response the HTTP response to set cookies for JWT tokens
     * @return a SignInResponse containing JWT tokens and user details
     */
    SignInResponse signIn(SignInRequest signInRequest, HttpServletResponse response);

    /**
     * Refreshes the JWT token.
     *
     * @param request the HTTP request containing the refresh token
     * @param response the HTTP response to set cookies for new JWT tokens
     * @return a JwtResponse containing the new JWT tokens
     */
    SignInResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    /**
     * Logs out the user by invalidating the refresh token.
     *
     * @param request the refresh token to be invalidated
     * @param response the HTTP response to clear cookies
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
