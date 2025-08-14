/*
 * @ {#} AuthController.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.services.AuthService;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUpForCustomer(@Valid @RequestBody SignUpRequest signUpRequest){
        // Call the service to handle sign-up logic
        boolean isSignedUp = authService.signUpForCustomer(signUpRequest);

        if (isSignedUp) {
            return ResponseEntity.ok(ApiResponse.noContent("Sign-up successful. Please log in to continue."));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sign-up failed. Please try again.", 400));
        }
    }
}
