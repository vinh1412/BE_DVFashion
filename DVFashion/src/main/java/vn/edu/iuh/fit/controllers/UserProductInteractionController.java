/*
 * @ {#} UserProductInteractionService.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.UserProductInteractionResponse;
import vn.edu.iuh.fit.enums.InteractionType;
import vn.edu.iuh.fit.services.impl.UserInteractionServiceImpl;

import java.time.LocalDate;

/*
 * @description: REST controller for managing user-product interactions.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/user-product-interactions")
@RequiredArgsConstructor
public class UserProductInteractionController {
    private final UserInteractionServiceImpl userInteractionService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserProductInteractionResponse>>> getAllUserProductInteractions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) InteractionType interactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, // yyyy-MM-dd
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate, // yyyy-MM-dd
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<UserProductInteractionResponse> interactions = userInteractionService.findAllWithFilters(
                userId, productId, interactionType, fromDate, toDate, page, size);

        return ResponseEntity.ok(ApiResponse.success(interactions, "User-Product Interactions retrieved successfully"));
    }
}
