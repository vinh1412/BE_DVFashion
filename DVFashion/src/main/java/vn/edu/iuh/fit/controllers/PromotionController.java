/*
 * @ {#} PromotionController.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CreatePromotionRequest;
import vn.edu.iuh.fit.dtos.request.UpdatePromotionRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.PromotionResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.services.PromotionService;
import vn.edu.iuh.fit.validators.ValidationGroups;

import java.util.List;

/*
 * @description: REST controller for managing promotions
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createPromotion(
            @Valid  @RequestPart("promotion") CreatePromotionRequest createPromotionRequest,
            @RequestParam(defaultValue = "VI") Language inputLang,
            @RequestPart(value = "bannerFile", required = false) MultipartFile bannerFile) {
        PromotionResponse response = promotionService.createPromotion(createPromotionRequest, inputLang, bannerFile);
        return ResponseEntity.ok(ApiResponse.created(response, "Promotion created successfully."));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping(value = "/{id}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updatePromotion(
            @Valid @RequestPart("promotion") UpdatePromotionRequest updatePromotionRequest,
            @PathVariable Long id,
            @RequestParam(defaultValue = "VI") Language inputLang,
            @RequestPart(value = "bannerFile", required = false) MultipartFile bannerFile) {
        PromotionResponse response = promotionService.updatePromotion(updatePromotionRequest, id, inputLang, bannerFile);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotion updated successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPromotionById(
            @PathVariable Long id,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PromotionResponse response = promotionService.getPromotionById(id, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotion retrieved successfully."));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllPromotionsNoPaging(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        List<PromotionResponse> promotions = promotionService.getAllPromotions(language);
        return ResponseEntity.ok(ApiResponse.success(promotions, "Promotions retrieved successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPromotionsPaging(
            @PageableDefault(page = 0, size = 12) Pageable pageable,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        PageResponse<PromotionResponse> response = promotionService.getPromotionsPaging(pageable, language);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotions retrieved successfully."));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<?>> getPromotionById(
//            @PathVariable Long id,
//            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
//        PromotionResponse response = promotionService.getPromotionById(id, language);
//        return ResponseEntity.ok(ApiResponse.success(response, "Promotion retrieved successfully."));
//    }
//
//    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<?>> updatePromotion(
//            @Validated(ValidationGroups.Update.class) @RequestBody CreatePromotionRequest createPromotionRequest,
//            @PathVariable Long id,
//            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
//        PromotionResponse response = promotionService.updatePromotion(createPromotionRequest, id, language);
//        return ResponseEntity.ok(ApiResponse.success(response, "Promotion updated successfully."));
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<?>> getAllPromotionsNoPaging(
//            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
//        List<PromotionResponse> promotions = promotionService.getAllPromotions(language);
//        return ResponseEntity.ok(ApiResponse.success(promotions, "Promotions retrieved successfully."));
//    }
}
