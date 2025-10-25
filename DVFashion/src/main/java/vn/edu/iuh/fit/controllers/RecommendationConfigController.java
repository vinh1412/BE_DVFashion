/*
 * @ {#} RecommendationConfigController.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.RecommendationConfigResponse;
import vn.edu.iuh.fit.services.RecommendationConfigService;

import java.util.List;

/*
 * @description: Controller for managing recommendation configurations
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/config/recommendations")
@RequiredArgsConstructor
public class RecommendationConfigController {
    private final RecommendationConfigService configService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationConfigResponse>>> getAllConfigs() {
        List<RecommendationConfigResponse> configs = configService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs, "Recommendation configs retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<RecommendationConfigResponse>> updateConfig(
            @PathVariable String key,
            @RequestParam String value) {

        RecommendationConfigResponse updated = configService.updateConfig(key, value);
        return ResponseEntity.ok(ApiResponse.success(updated, "Config updated successfully"));
    }
}
