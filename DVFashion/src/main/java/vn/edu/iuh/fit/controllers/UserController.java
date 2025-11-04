/*
 * @ {#} UserController.java   1.0     01/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.ChangePasswordRequest;
import vn.edu.iuh.fit.dtos.request.CreateStaffRequest;
import vn.edu.iuh.fit.dtos.request.UserRequest;
import vn.edu.iuh.fit.dtos.request.VerifyStaffRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.UserService;

import java.util.List;

/*
 * @description: REST controller for managing user-related operations
 * @author: Tran Hien Vinh
 * @date:   01/09/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable("id") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @GetMapping
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.noContent("Password changed successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<?>> deleteAccount() {
        userService.softDeleteAccount();
        return ResponseEntity.ok(ApiResponse.noContent("Account deleted successfully. You can restore it after 30 days by registering again."));
    }

    @PostMapping
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<UserResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request) {
        UserResponse staff = userService.createStaff(request);
        return ResponseEntity.ok(ApiResponse.success(staff, "Employee account created successfully. Verification email sent."));
    }

    @PostMapping("/verify-staff")
    public ResponseEntity<ApiResponse<UserResponse>> verifyStaff(
            @Valid @RequestBody VerifyStaffRequest request) {
        UserResponse staff = userService.verifyStaff(request);
        return ResponseEntity.ok(ApiResponse.success(staff, "Employee account verified successfully"));
    }
}
