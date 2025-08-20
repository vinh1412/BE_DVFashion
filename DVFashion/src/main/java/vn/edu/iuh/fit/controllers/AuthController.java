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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.RefreshTokenRequest;
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

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUpForCustomer(@Valid @RequestBody SignUpRequest signUpRequest){
        try {
            boolean isSuccess = authService.signUpForCustomer(signUpRequest);
            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.noContent("Sign up successful."));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Sign up failed.", 400));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
        try {
            SignInResponse signInResponse = authService.signIn(signInRequest, response);
            return ResponseEntity.ok(ApiResponse.success(signInResponse, "Sign in successful."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage(), 400));
        }
    }

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

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_STAFF_CUSTOMER)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request,
                                                 HttpServletResponse response) {
        try {
            authService.logout(request, response);
            return ResponseEntity.ok(ApiResponse.noContent("Logout successful."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.notFound("Error: " + e.getMessage()));
        }
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(){
//        try {
            UserResponse user = userService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Error: " + e.getMessage(), 401));
//        }
    }
}
