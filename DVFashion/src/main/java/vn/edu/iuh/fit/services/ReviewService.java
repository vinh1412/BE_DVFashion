/*
 * @ {#} ReviewService.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.*;
;

import java.util.List;

/*
 * @description: Service interface for managing product reviews.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public interface ReviewService {
    /**
     * Creates a new product review.
     *
     * @param request    the review creation request
     * @param imageFiles the list of image files to be uploaded with the review
     * @return the created review response
     */
    ReviewResponse createReview(CreateReviewRequest request, List<MultipartFile> imageFiles);

    /**
     * Checks if a user can review a specific product variant from an order.
     *
     * @param orderId          the ID of the order
     * @param productVariantId the ID of the product variant
     * @return true if the user can review the product, false otherwise
     */
    boolean canUserReviewProduct(Long orderId, Long productVariantId);

    /**
     * Updates an existing product review.
     *
     * @param reviewId   the ID of the review to be updated
     * @param request    the review update request
     * @param imageFiles the list of new image files to be uploaded with the review
     * @return the updated review response
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, List<MultipartFile> imageFiles);

    /**
     * Checks if a review can be edited.
     *
     * @param reviewId the ID of the review
     * @return true if the review can be edited, false otherwise
     */
    boolean canEditReview(Long reviewId);

    /**
     * Retrieves all reviews for a specific product.
     *
     * @param productId the ID of the product
     * @return a list of review responses for the product
     */
    List<ReviewResponse> getProductReviews(Long productId);

    /**
     * Moderates a review based on the provided moderation request.
     *
     * @param reviewId the ID of the review to be moderated
     * @param request  the moderation request containing moderation details
     * @return the moderated review response
     */
    ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request);

    /**
     * Get all reviews for admin with filtering options.
     * @param filterRequest the filter request containing filtering criteria
     * @return the admin reviews response with filtered reviews
     */
    AdminReviewsResponse getAllReviewsForAdmin(AdminReviewFilterRequest filterRequest);

    /**
     * Gets product reviews with filtering options.
     * @param productId the ID of the product
     * @param filterRequest the filter request containing filtering criteria
     * @return the product reviews response with filtered reviews
     */
    ProductReviewsResponse getProductReviews(Long productId, ProductReviewFilterRequest filterRequest);

    /**
     * Retrieves all reviews for the current customer.
     *
     * @return a list of review responses for the current customer
     */
    List<ReviewResponse> getAllReviewsForCustomer();

    /**
     * Creates a reply to a review.
     *
     * @param request the review reply creation request
     * @return the created review reply response
     */
    ReviewReplyResponse createReviewReply(CreateReviewReplyRequest request);

    /**
     * Updates an existing review reply.
     *
     * @param replyId the ID of the review reply to be updated
     * @param request the review reply update request
     * @return the updated review reply response
     */
    ReviewReplyResponse updateReviewReply(Long replyId, UpdateReviewReplyRequest request);

    /**
     * Deletes a review reply.
     *
     * @param replyId the ID of the review reply to be deleted
     */
    void deleteReviewReply(Long replyId);

    /**
     * Retrieves all review replies for a specific review for the current customer.
     *
     * @param reviewId the ID of the review
     * @return a list of review reply responses for the review
     */
    List<ReviewReplyResponse> getReviewRepliesForCustomer(Long reviewId);

    /**
     * Retrieves all review replies for a specific review for admin.
     *
     * @param reviewId the ID of the review
     * @return a list of review reply responses for the review
     */
    List<ReviewReplyResponse> getAllReviewRepliesForAdmin(Long reviewId);

    /**
     * Moderates a review reply based on the provided moderation request.
     *
     * @param replyId the ID of the review reply to be moderated
     * @param request the moderation request containing moderation details
     * @return the moderated review reply response
     */
    ReviewReplyResponse moderateReviewReply(Long replyId, ModerateReviewReplyRequest request);
}
