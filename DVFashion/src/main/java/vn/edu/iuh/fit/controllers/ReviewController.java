/*
 * @ {#} ReviewController.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.AdminReviewsResponse;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ProductReviewsResponse;
import vn.edu.iuh.fit.dtos.response.ReviewResponse;
import vn.edu.iuh.fit.services.ReviewService;

import java.util.List;

/*
 * @description: REST controller for managing product reviews.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestPart("review") CreateReviewRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        ReviewResponse response = reviewService.createReview(request, imageFiles);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Review created successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @GetMapping("/can-review")
    public ResponseEntity<ApiResponse<Boolean>> canReviewProduct(
            @RequestParam Long orderId,
            @RequestParam Long productVariantId) {

        boolean canReview = reviewService.canUserReviewProduct(orderId, productVariantId);

        return ResponseEntity.ok(ApiResponse.success(canReview, "Review eligibility checked"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart(value = "review", required = false) UpdateReviewRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        ReviewResponse response = reviewService.updateReview(reviewId, request, imageFiles);

        return ResponseEntity.ok(ApiResponse.success(response, "Review updated successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @GetMapping("/{reviewId}/can-edit")
    public ResponseEntity<ApiResponse<Boolean>> canEditReview(@PathVariable Long reviewId) {
        boolean canEdit = reviewService.canEditReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(canEdit, "Edit eligibility checked"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductReviewsResponse>> getProductReviewsFilter(
            @PathVariable Long productId,
            @Valid ProductReviewFilterRequest filterRequest) {

        ProductReviewsResponse response = reviewService.getProductReviews(productId, filterRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Product reviews retrieved successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ModerateReviewRequest request) {

        ReviewResponse response = reviewService.moderateReview(reviewId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Review moderated successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<AdminReviewsResponse>> getAllReviewsForAdmin(
            @Valid AdminReviewFilterRequest filterRequest) {

        AdminReviewsResponse response = reviewService.getAllReviewsForAdmin(filterRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "All reviews retrieved successfully"));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews() {
        List<ReviewResponse> responses = reviewService.getAllReviewsForCustomer();
        return ResponseEntity.ok(ApiResponse.success(responses, "My reviews retrieved successfully"));
    }
}
