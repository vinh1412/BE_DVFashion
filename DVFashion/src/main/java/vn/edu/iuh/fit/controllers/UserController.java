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


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable("id") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @PostMapping
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<?>> createStaff(
            @Valid @RequestBody CreateStaffRequest request) {
        UserResponse staff = userService.createStaff(request);
        return ResponseEntity.ok(ApiResponse.success(staff, "Employee account created successfully. Verification email sent."));
    }

    @PostMapping("/verify-staff")
    public ResponseEntity<ApiResponse<?>> verifyStaff(
            @Valid @RequestBody VerifyStaffRequest request) {
        UserResponse staff = userService.verifyStaff(request);
        return ResponseEntity.ok(ApiResponse.success(staff, "Employee account verified successfully"));
    }

    @GetMapping
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
