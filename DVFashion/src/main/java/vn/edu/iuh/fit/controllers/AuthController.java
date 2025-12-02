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
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.EmailService;
import vn.edu.iuh.fit.services.OtpAuthService;
import vn.edu.iuh.fit.services.UserService;

import java.util.Map;


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

    private final EmailService emailService;

    private final OtpAuthService otpAuthService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUpForCustomer(@Valid @RequestBody SignUpRequest signUpRequest){
        boolean isSuccess = authService.signUpForCustomer(signUpRequest);
        if (isSuccess) {
            return ResponseEntity.ok(ApiResponse.noContent("Sign up successful."));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sign up failed.", 400));
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

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request,
                                                 HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.noContent("Logout successful."));

    }

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(){
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        emailService.sendPasswordResetEmail(request);
        return ResponseEntity.ok(ApiResponse.noContent("Password reset email has been sent!"));
    }

    @GetMapping("/password/{token}")
    public ResponseEntity<ApiResponse<?>> validateToken(@PathVariable("token") String token) {
        User user = emailService.validatePasswordResetToken(token);
        return ResponseEntity.ok(ApiResponse.success(user.getId(), "Token is valid."));
    }

    @PostMapping("/reset-password-mail")
    public ResponseEntity<ApiResponse<?>> resetPasswordMail(@Valid @RequestBody ResetPasswordMailRequest request) {
        emailService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.noContent("Password has been reset successfully."));
    }

    @PostMapping("/verify-otp-forgot-password")
    public ResponseEntity<ApiResponse<?>> verifyOtpForgotPassword(@Valid @RequestBody VerifyOtpRequest request) {
        String phoneNumber = otpAuthService.verifyOtpForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Phone number "+phoneNumber+" verification successful!"));
    }

    @PostMapping("/reset-password-otp")
    public ResponseEntity<ApiResponse<?>> resetPasswordOTP(@Valid @RequestBody ResetPasswordOtpRequest request) {
        otpAuthService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.noContent("Password has been reset successfully."));
    }

    @PostMapping("/verify-otp-sign-up")
    public ResponseEntity<ApiResponse<?>> verifyOTPForSignUp(@Valid @RequestBody VerifyOtpRequest request) {
        String phoneNumber = otpAuthService.verifyOtpForSignUp(request);
        return ResponseEntity.ok(ApiResponse.success("Phone number "+phoneNumber+" verification successful!"));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK");
    }
}
