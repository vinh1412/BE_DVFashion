/*
 * @ {#} PromotionController.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.PromotionRequest;
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
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(
            @Validated(ValidationGroups.Create.class) @RequestBody PromotionRequest promotionRequest,
            @RequestParam(defaultValue = "VI") Language inputLang) {
        PromotionResponse response = promotionService.createPromotion(promotionRequest, inputLang);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(
            @PathVariable Long id,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PromotionResponse response = promotionService.getPromotionById(id, language);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @Validated(ValidationGroups.Update.class) @RequestBody PromotionRequest promotionRequest,
            @PathVariable Long id,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        PromotionResponse response = promotionService.updatePromotion(promotionRequest, id, language);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PromotionResponse>> getAllPromotionsNoPaging(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        List<PromotionResponse> promotions = promotionService.getAllPromotions(language);
        return ResponseEntity.ok(promotions);
    }
}
