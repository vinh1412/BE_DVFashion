/*
 * @ {#} RecommendationModelVersionController.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CreateRecommendationModelVersionRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.RecommendationModelVersionResponse;
import vn.edu.iuh.fit.services.RecommendationModelVersionService;

import java.util.List;

/*
 * @description: Controller for managing recommendation model versions
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/models/recommendations")
@RequiredArgsConstructor
public class RecommendationModelVersionController {
    private final RecommendationModelVersionService modelService;

    @PreAuthorize((RoleConstant.HAS_ROLE_ADMIN))
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationModelVersionResponse>>> getAllModels() {
        return ResponseEntity.ok(ApiResponse.success(modelService.getAllVersions(), "Models retrieved successfully"));
    }

    @PreAuthorize((RoleConstant.HAS_ROLE_ADMIN))
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateModel(@PathVariable Long id) {
        modelService.activateModel(id);
        return ResponseEntity.ok(ApiResponse.success("Activated model version " + id));
    }

    @PreAuthorize((RoleConstant.HAS_ROLE_ADMIN))
    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<RecommendationModelVersionResponse>> evaluateModel(
            @RequestBody CreateRecommendationModelVersionRequest request
    ) {
        RecommendationModelVersionResponse result = modelService.evaluateModel(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Model evaluated and saved successfully"));
    }
}
