/*
 * @ {#} UserController.java   1.0     01/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.UserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.UserService;

/*
 * @description:
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
}
