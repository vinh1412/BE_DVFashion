/*
 * @ {#} ReviewReplyController.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CreateReviewReplyRequest;
import vn.edu.iuh.fit.dtos.request.ModerateReviewReplyRequest;
import vn.edu.iuh.fit.dtos.request.UpdateReviewReplyRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ReviewReplyResponse;
import vn.edu.iuh.fit.services.ReviewService;

import java.util.List;

/*
 * @description: REST controller for managing review replies.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/review-replies")
@RequiredArgsConstructor
public class ReviewReplyController {
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    public ResponseEntity<ApiResponse<ReviewReplyResponse>> createReviewReply(
            @Valid @RequestBody CreateReviewReplyRequest request) {

        ReviewReplyResponse response = reviewService.createReviewReply(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Review reply created successfully"));
    }

    @PutMapping("/{replyId}")
    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    public ResponseEntity<ApiResponse<ReviewReplyResponse>> updateReviewReply(
            @PathVariable Long replyId,
            @Valid @RequestBody UpdateReviewReplyRequest request) {

        ReviewReplyResponse response = reviewService.updateReviewReply(replyId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Review reply updated successfully")
        );
    }

    @DeleteMapping("/{replyId}")
    @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_CUSTOMER)
    public ResponseEntity<ApiResponse<Void>> deleteReviewReply(@PathVariable Long replyId) {

        reviewService.deleteReviewReply(replyId);

        return ResponseEntity.ok(
                ApiResponse.noContent("Review reply deleted successfully")
        );
    }

    @GetMapping("/review/{reviewId}/customer")
    public ResponseEntity<ApiResponse<List<ReviewReplyResponse>>> getReviewRepliesByReviewIdForCustomer(
            @PathVariable Long reviewId) {

        List<ReviewReplyResponse> responses = reviewService.getReviewRepliesForCustomer(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(responses, "Review replies retrieved successfully")
        );
    }

    @GetMapping("/review/{reviewId}/admin")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<ReviewReplyResponse>>> getAllReviewRepliesByReviewIdForAdmin(
            @PathVariable Long reviewId) {

        List<ReviewReplyResponse> responses = reviewService.getAllReviewRepliesForAdmin(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(responses, "All review replies retrieved successfully")
        );
    }

    @PutMapping("/{replyId}/moderate")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<ReviewReplyResponse>> moderateReviewReply(
            @PathVariable Long replyId,
            @Valid @RequestBody ModerateReviewReplyRequest request) {

        ReviewReplyResponse response = reviewService.moderateReviewReply(replyId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Review reply moderated successfully")
        );
    }

}
