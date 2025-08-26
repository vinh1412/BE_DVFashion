/*
 * @ {#} AuthController.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.UserService;

/*
 * @description: Controller for handling authentication requests
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/auth")
public class AuthController {
    private final AuthService authService;

    private final UserService userService;

    /**
     * API for customer sign up
     *
     * HOW TO TEST WITH POSTMAN:
     *
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/auth/sign-up
     *
     * 3. BODY (select raw - JSON):
     *    {
     *      "email": "user@example.com",
     *      "password": "password123",
     *      "fullName": "John Doe",
     *      "phone": "0123456789"
     *    }
     *
     * 4. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "statusCode": 204,
     *      "message": "Sign up successful.",
     *    }
     *
     * COMMON ERRORS:
     * - 400: Bad Request - Invalid input data or validation failed
     * - 409: Conflict - Email already exists
     * - 400: Bad Request - Invalid email format
     */
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUpForCustomer(@Valid @RequestBody SignUpRequest signUpRequest){
        boolean isSuccess = authService.signUpForCustomer(signUpRequest);
        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.noContent("Sign up successful."));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sign up failed.", 400));
        }
    }


    /**
     * API for user sign in
     *
     * HOW TO TEST WITH POSTMAN:
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/auth/sign-in
     *
     * 3. BODY (select raw - JSON):
     *   {
     *     "username" : "test@gmail.com",
     *     "password" : "12345678"
     *   }
     *
     *  4. SUCCESS RESPONSE (200):
     *  {
     *   "success": true,
     *   "statusCode": 200,
     *   "message": "Sign in successful.",
     *   "data": {
     *       "id": 1,
     *       "email": "test@gmail.com",
     *       "phone": "+84123456789",
     *        "roles": [
     *             "ROLE_CUSTOMER"
     *         ]
     *     }
     *  }
     *
     *  COMMON ERRORS:
     *  - 400: Bad Request - Invalid input data or validation failed
     *  - 401: Unauthorized - Invalid username or password
     *  - 400: Bad Request - Account is disabled. Please contact support.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
        try {
            SignInResponse signInResponse = authService.signIn(signInRequest, response);
            return ResponseEntity.ok(ApiResponse.success(signInResponse, "Sign in successful."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage(), 400));
        }
    }

    /**
     * API to refresh JWT token
     *
     * HOW TO TEST WITH POSTMAN:
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/auth/refresh-token
     *
     * 3. RESPONSE (200):
     * {
     *     "success": true,
     *     "statusCode": 200,
     *     "message": "Token refreshed successfully.",
     *     "data": {
     *         "id": 1,
     *         "email": "admin@gmail.com",
     *         "phone": "+84123456789",
     *         "roles": [
     *             "ROLE_CUSTOMER",
     *             "ROLE_STAFF",
     *             "ROLE_ADMIN"
     *         ]
     *     }
     * }
     *
     * COMMON ERRORS:
     * - 400: Bad Request - Cookies are missing or invalid
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(HttpServletRequest request,
                                                       HttpServletResponse response) {
        try {
            SignInResponse signInResponse = authService.refreshToken(request, response);
            return ResponseEntity.ok(ApiResponse.success(signInResponse, "Token refreshed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error: " + e.getMessage(), 400));
        }
    }

    /**
     * API for user logout
     *
     * HOW TO TEST WITH POSTMAN:
     * 1. METHOD: POST
     * 2. URL: http://localhost:8080/api/v1/auth/logout
     *
     * 3. SUCCESS RESPONSE (200):
     *    {
     *      "success": true,
     *      "statusCode": 204,
     *      "message": "Logout successful."
     *    }
     *
     * COMMON ERRORS:
     * - 400: Bad Request - Access Denied
     * - 400: Bad Request - Refresh token is missing
     */
    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_STAFF_CUSTOMER)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request,
                                                 HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.noContent("Logout successful."));

    }


    /**
     * API to get current logged-in user
     *
     * HOW TO TEST WITH POSTMAN:
     * 1. METHOD: GET
     * 2. URL: http://localhost:8080/api/v1/auth/me
     *
     * 3. SUCCESS RESPONSE (200):
     * {
     *     "success": true,
     *     "statusCode": 200,
     *     "message": "User retrieved successfully.",
     *     "data": {
     *         "id": 1,
     *         "email": "cus123@gmail.com",
     *         "fullName": "cus123",
     *         "phone": "+84123456789",
     *         "dob": "2003-01-01",
     *         "gender": "MALE",
     *         "roles": [
     *             "ROLE_CUSTOMER"
     *         ]
     *     }
     * }
     *
     * COMMON ERRORS:
     * - 401: Unauthorized - User is not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(){
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully."));
    }
}
