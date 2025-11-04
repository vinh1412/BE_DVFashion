/*
 * @ {#} ReviewServiceImpl.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.mappers.ReviewMapper;
import vn.edu.iuh.fit.mappers.ReviewReplyMapper;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.services.*;
import vn.edu.iuh.fit.specifications.ReviewSpecification;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/*
 * @description: Service implementation for managing product reviews.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    private final OrderRepository orderRepository;

    private final ProductVariantRepository productVariantRepository;

    private final UserRepository userRepository;

    private final CloudinaryService cloudinaryService;

    private final TranslationService translationService;

    private final UserService userService;

    private final ContentModerationService contentModerationService;

    private final ReviewMapper reviewMapper;

    private final UserInteractionService userInteractionService;

    private final ReviewReplyRepository reviewReplyRepository;

    private final ReviewReplyTranslationRepository reviewReplyTranslationRepository;

    private final ReviewReplyMapper reviewReplyMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, List<MultipartFile> imageFiles) {
        // Get current user
        Long currentUserId = userService.getCurrentUser().getId();

        // Validate user exists
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validate order exists and belongs to user
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Validate order ownership
        if (!order.getCustomer().getId().equals(currentUserId)) {
            throw new BadRequestException("You can only review your own orders");
        }

        // Validate order is delivered
        if (!OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new BadRequestException("You can only review delivered orders");
        }

        // Validate product variant exists and is in the order
        ProductVariant productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new NotFoundException("Product variant not found"));

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProductVariant().getId().equals(request.productVariantId()));

        // Validate product variant is in the order
        if (!productInOrder) {
            throw new BadRequestException("Product variant not found in this order");
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndOrderIdAndProductVariantId(
                currentUserId, request.orderId(), request.productVariantId());

        // Validate user hasn't already reviewed this product for this order
        if (alreadyReviewed) {
            throw new BadRequestException("You have already reviewed this product for this order");
        }

        // Create review entity
        Review review = Review.builder()
                .user(user)
                .order(order)
                .productVariant(productVariant)
                .rating(request.rating())
                .helpfulCount(0)
                .images(new ArrayList<>())
                .translations(new ArrayList<>())
                .status(ReviewStatus.PENDING)
                .build();

        review = reviewRepository.save(review);

        // Handle image uploads
        if (imageFiles != null && !imageFiles.isEmpty()) {
            validateImageFiles(imageFiles);

            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(imageFile);
                    ReviewImage reviewImage = ReviewImage.builder()
                            .review(review)
                            .imageUrl(imageUrl)
                            .build();
                    review.getImages().add(reviewImage);
                }
            }
        }

        // Create translations for both languages
        if (request.comment() != null && !request.comment().trim().isEmpty()) {
            String inputComment = request.comment().trim();

            Language inputLanguage = LanguageUtils.getCurrentLanguage();

            // Create translation for input language
            ReviewTranslation inputTranslation = ReviewTranslation.builder()
                    .review(review)
                    .language(inputLanguage)
                    .comment(inputComment)
                    .build();
            review.getTranslations().add(inputTranslation);

            // Create translation for the other language
            Language targetLanguage = (inputLanguage == Language.VI) ? Language.EN : Language.VI;
            String translatedComment = translationService.translate(inputComment, targetLanguage.name().toLowerCase());

            if (translatedComment != null && !translatedComment.trim().isEmpty()) {
                ReviewTranslation targetTranslation = ReviewTranslation.builder()
                        .review(review)
                        .language(targetLanguage)
                        .comment(translatedComment.trim())
                        .build();
                review.getTranslations().add(targetTranslation);
            }
        }

        // Save the complete review with translations and images
        review = reviewRepository.save(review);

        // Track REVIEW interaction
        try {
            userInteractionService.trackInteraction(
                    user.getId(),
                    productVariant.getProduct().getId(),
                    InteractionType.REVIEW,
                    BigDecimal.valueOf(review.getRating())
            );
        } catch (Exception e) {
            log.warn("Could not track REVIEW interaction: {}", e.getMessage());
        }

        // Auto-moderate review with AI
        ReviewStatus autoModeratedStatus = autoModerateReview(review, review.getTranslations());

        // Set the auto-moderated status
        review.setStatus(autoModeratedStatus);

        // Save final status
        review = reviewRepository.save(review);

        return reviewMapper.mapToResponse(review);
    }

    @Override
    public boolean canUserReviewProduct(Long orderId, Long productVariantId) {
        Long userId = userService.getCurrentUser().getId();
        // Check if order exists and belongs to user
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getCustomer().getId().equals(userId)) {
            return false;
        }

        // Check if order is delivered
        if (!OrderStatus.DELIVERED.equals(order.getStatus())) {
            return false;
        }

        // Check if product variant is in the order
        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProductVariant().getId().equals(productVariantId));

        if (!productInOrder) {
            return false;
        }

        // Check if user hasn't already reviewed this product for this order
        return !reviewRepository.existsByUserIdAndOrderIdAndProductVariantId(
                userId, orderId, productVariantId);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, List<MultipartFile> imageFiles) {
        // Get current user and language
        Long currentUserId = userService.getCurrentUser().getId();
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Find existing review
        Review existsReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        // Validate ownership
        if (!existsReview.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        // Validate review is not hidden
        if (existsReview.getStatus() == ReviewStatus.HIDDEN) {
            throw new BadRequestException("This review has been hidden and cannot be edited");
        }

        // Validate update time limit (3 days)
        if (existsReview.getCreatedAt().isBefore(LocalDateTime.now().minusDays(3))) {
            throw new BadRequestException("You can only update reviews within 3 days of creation");
        }

        // Validate review status for editing
        switch (existsReview.getStatus()) {
            case PENDING, NEED_REVIEW, AUTO_APPROVED, APPROVED -> existsReview.setStatus(ReviewStatus.PENDING); // Allow editing and reset status to PENDING
            case REJECTED -> throw new BadRequestException("Cannot edit a rejected review");
            default -> throw new BadRequestException("Cannot edit this review");
        }

        if (request != null) {
            // Update rating if provided
            if (request.rating() != null) {
                existsReview.setRating(request.rating());
            }

            // Handle image deletions
            if (request.imagesToDelete() != null && !request.imagesToDelete().isEmpty()) {
                List<ReviewImage> currentImages = existsReview.getImages();

                // Check if all URLs to delete exist in current images
                List<String> invalidUrls = request.imagesToDelete().stream()
                        .filter(url -> currentImages.stream().noneMatch(img -> img.getImageUrl().equals(url)))
                        .toList();

                // If any URL is invalid, throw exception
                if (!invalidUrls.isEmpty()) {
                    throw new BadRequestException("Some images do not belong to this review: " + invalidUrls);
                }

                // Filter images to remove
                List<ReviewImage> imagesToRemove = currentImages.stream()
                        .filter(img -> request.imagesToDelete().contains(img.getImageUrl()))
                        .toList();

                // Remove images
                for (ReviewImage image : imagesToRemove) {
                    // cloudinaryService.deleteImage(image.getImageUrl());
                    existsReview.getImages().remove(image);
                }
            }

            // Handle new image uploads
            if (imageFiles != null && !imageFiles.isEmpty()) {
                validateImageFiles(imageFiles);

                // Check total image count after adding new ones
                int currentImageCount = existsReview.getImages().size();
                if (currentImageCount + imageFiles.size() > 5) {
                    throw new BadRequestException("Total images cannot exceed 5");
                }

                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(imageFile);
                        ReviewImage reviewImage = ReviewImage.builder()
                                .review(existsReview)
                                .imageUrl(imageUrl)
                                .build();
                        existsReview.getImages().add(reviewImage);
                    }
                }
            }

            // Update translations if comment is provided
            if (request.comment() != null && !request.comment().trim().isEmpty()) {
                String inputComment = request.comment().trim();

                // Update or create translation for current language
                ReviewTranslation currentTranslation = existsReview.getTranslations().stream()
                        .filter(t -> t.getLanguage() == currentLanguage)
                        .findFirst()
                        .orElseGet(() -> {
                            ReviewTranslation newTranslation = ReviewTranslation.builder()
                                    .review(existsReview)
                                    .language(currentLanguage)
                                    .build();
                            existsReview.getTranslations().add(newTranslation);
                            return newTranslation;
                        });
                currentTranslation.setComment(inputComment);

                // Update or create translation for the other language
                Language targetLanguage = (currentLanguage == Language.VI) ? Language.EN : Language.VI;
                String translatedComment = translationService.translate(inputComment, targetLanguage.name().toLowerCase());

                if (translatedComment != null && !translatedComment.trim().isEmpty()) {
                    ReviewTranslation targetTranslation = existsReview.getTranslations().stream()
                            .filter(t -> t.getLanguage() == targetLanguage)
                            .findFirst()
                            .orElseGet(() -> {
                                ReviewTranslation newTranslation = ReviewTranslation.builder()
                                        .review(existsReview)
                                        .language(targetLanguage)
                                        .build();
                                existsReview.getTranslations().add(newTranslation);
                                return newTranslation;
                            });
                    targetTranslation.setComment(translatedComment.trim());
                }
            }
        }

        // Mark review as edited
        existsReview.setEdited(true);
        existsReview.setEditedAt(LocalDateTime.now());

        // Auto-moderate the updated review
        ReviewStatus autoModeratedStatus = autoModerateReview(existsReview, existsReview.getTranslations());

        // Set the auto-moderated status
        existsReview.setStatus(autoModeratedStatus);

        // Save the updated review
        Review review = reviewRepository.save(existsReview);

        return reviewMapper.mapToResponse(review);
    }

    @Override
    public boolean canEditReview(Long reviewId) {
        Long currentUserId = userService.getCurrentUser().getId();

        // Find existing review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        // Validate ownership
        if (!review.getUser().getId().equals(currentUserId)) {
            return false;
        }

        // Validate update time limit (3 days)
        return review.getCreatedAt().isAfter(LocalDateTime.now().minusDays(3));
    }

    @Override
    public List<ReviewResponse> getProductReviews(Long productId) {
        // Validate product exists
        if (!productVariantRepository.existsByProductId(productId)) {
            throw new NotFoundException("Product not found");
        }

        // Only show approved and auto-approved reviews
        List<ReviewStatus> allowedStatuses = List.of(
                ReviewStatus.APPROVED,
                ReviewStatus.AUTO_APPROVED
        );

        // Get all reviews
        List<Review> reviews = reviewRepository.findByProductIdAndStatusIn(productId, allowedStatuses);

        // Map to response with current language
        return reviews.stream()
                .map(reviewMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request) {
        // Find review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

        ReviewStatus currentStatus = review.getStatus();
        ReviewStatus newStatus = ReviewStatus.valueOf(request.newStatus());

        // Validate newStatus not null
        if (newStatus == null) {
            throw new BadRequestException("New status cannot be null");
        }

        // Check if status is unchanged
        if (currentStatus == newStatus) {
            throw new BadRequestException("Review is already in the '" + newStatus + "' state");
        }

        // Check valid status transitions
        validateStatusTransition(currentStatus, newStatus);

        // Update status
        review.setStatus(newStatus);

        // Require admin comment if rejecting or hiding
        if ((newStatus == ReviewStatus.REJECTED || newStatus == ReviewStatus.HIDDEN)
                && (request.adminComment() == null || request.adminComment().isBlank())) {
            throw new BadRequestException("Admin comment is required when rejecting or hiding a review");
        }

        // Handle admin comment with translations
        if (request.adminComment() != null && !request.adminComment().isBlank()) {
            String inputComment = request.adminComment().trim();
            Language inputLanguage = LanguageUtils.getCurrentLanguage();

            // Update or create translation for input language
            ReviewTranslation inputTranslation = review.getTranslations().stream()
                    .filter(t -> t.getLanguage() == inputLanguage)
                    .findFirst()
                    .orElseGet(() -> {
                        ReviewTranslation newTranslation = ReviewTranslation.builder()
                                .review(review)
                                .language(inputLanguage)
                                .build();
                        review.getTranslations().add(newTranslation);
                        return newTranslation;
                    });
            inputTranslation.setAdminComment(inputComment);

            // Update or create translation for the other language
            Language targetLanguage = (inputLanguage == Language.VI) ? Language.EN : Language.VI;
            String translatedComment = translationService.translate(inputComment, targetLanguage.name().toLowerCase());

            if (translatedComment != null && !translatedComment.isBlank()) {
                ReviewTranslation targetTranslation = review.getTranslations().stream()
                        .filter(t -> t.getLanguage() == targetLanguage)
                        .findFirst()
                        .orElseGet(() -> {
                            ReviewTranslation newTranslation = ReviewTranslation.builder()
                                    .review(review)
                                    .language(targetLanguage)
                                    .build();
                            review.getTranslations().add(newTranslation);
                            return newTranslation;
                        });
                targetTranslation.setAdminComment(translatedComment.trim());
            }
        }

        // Save updated review
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.mapToResponse(updatedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReviewsResponse getAllReviewsForAdmin(AdminReviewFilterRequest filterRequest) {
        // Validate filter request
        if (filterRequest == null) {
            filterRequest = new AdminReviewFilterRequest(null, "createdAt", "DESC");
        }

        // Create sort
        Sort sort = Sort.by(
                filterRequest.sortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                filterRequest.sortBy()
        );

        // Get reviews with filter
        List<Review> reviews = reviewRepository.findAllWithFilter(
                filterRequest.status(),
                sort
        );

        // Map to response
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewMapper::mapToResponse)
                .toList();

        // Get statistics
        ReviewStatisticsByStatus statistics = getReviewStatistics();

        return new AdminReviewsResponse(reviewResponses, statistics);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsResponse getProductReviews(Long productId, ProductReviewFilterRequest filterRequest) {
        // Validate product exists
        if (!productVariantRepository.existsByProductId(productId)) {
            throw new NotFoundException("Product not found");
        }

        // Define allowed statuses
        List<ReviewStatus> allowedStatuses = List.of(
                ReviewStatus.APPROVED,
                ReviewStatus.AUTO_APPROVED
        );

        // Build specification with filters
        Specification<Review> spec =
                ReviewSpecification.hasProductId(productId)
                        .and(ReviewSpecification.hasStatuses(allowedStatuses));

        if (filterRequest != null) {
            if (filterRequest.rating() != null) {
                // Validate rating range
                if (filterRequest.rating() < 1 || filterRequest.rating() > 5) {
                    throw new BadRequestException("Rating must be between 1 and 5");
                }
                spec = spec.and(ReviewSpecification.hasRating(filterRequest.rating()));
            }

            if (filterRequest.hasImages() != null && filterRequest.hasImages()) {
                spec = spec.and(ReviewSpecification.hasImages(true));
            }

            if (filterRequest.hasComment() != null && filterRequest.hasComment()) {
                spec = spec.and(ReviewSpecification.hasComment(true));
            }
        }

        // Get filtered reviews
        List<Review> reviews = reviewRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Map to response
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewMapper::mapToResponse)
                .toList();

        // Get statistics
        ProductReviewStatistics statistics = getProductReviewStatistics(productId, allowedStatuses);

        return new ProductReviewsResponse(reviewResponses, statistics);
    }

    @Override
    public List<ReviewResponse> getAllReviewsForCustomer() {
        // Get current user
        Long currentUserId = userService.getCurrentUser().getId();

        // Validate user exists
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Get all reviews belonging to the current user
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // Map to response DTOs
        return reviews.stream()
                .map(reviewMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewReplyResponse createReviewReply(CreateReviewReplyRequest request) {
        // Get current user
        UserResponse currentUser = userService.getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validate review exists
        Review review = reviewRepository.findById(request.reviewId())
                .orElseThrow(() -> new NotFoundException("Review not found"));

        // Validate parent reply if provided
        ReviewReply parentReply = null;
        if (request.parentReplyId() != null) {
            parentReply = reviewReplyRepository.findById(request.parentReplyId())
                    .orElseThrow(() -> new NotFoundException("Parent reply not found"));

            // Ensure parent reply belongs to the same review
            if(!request.reviewId().equals(parentReply.getReview().getId())) {
                throw new BadRequestException("Parent reply does not belong to the specified review");
            }

        }

        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Create review reply
        ReviewReply reviewReply = ReviewReply.builder()
                .review(review)
                .parentReply(parentReply)
                .user(user)
                .status(ReviewReplyStatus.APPROVED)
                .build();

        reviewReply = reviewReplyRepository.save(reviewReply);

        // Create translation for current language
        ReviewReplyTranslation currentTranslation = ReviewReplyTranslation.builder()
                .reviewReply(reviewReply)
                .language(currentLanguage)
                .content(request.content())
                .build();
        reviewReplyTranslationRepository.save(currentTranslation);

        // Create translation for other language
        Language otherLanguage = (currentLanguage == Language.VI) ? Language.EN : Language.VI;
        String translatedContent = translationService.translate(request.content(), otherLanguage.name());

        ReviewReplyTranslation otherTranslation = ReviewReplyTranslation.builder()
                .reviewReply(reviewReply)
                .language(otherLanguage)
                .content(translatedContent)
                .build();
        reviewReplyTranslationRepository.save(otherTranslation);

        if (reviewReply.getTranslations() == null) {
            reviewReply.setTranslations(new ArrayList<>());
        }

        reviewReply.getTranslations().add(currentTranslation);
        reviewReply.getTranslations().add(otherTranslation);

        return reviewReplyMapper.mapToReviewReplyResponse(reviewReply, currentLanguage);
    }

    @Override
    @Transactional
    public ReviewReplyResponse updateReviewReply(Long replyId, UpdateReviewReplyRequest request) {
        // Get current user
        UserResponse currentUser = userService.getCurrentUser();

        // Find reply
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Review reply not found"));

        // Check ownership (user can only edit their own replies, admin can edit any)
        if (!reply.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only edit your own replies");
        }

        if (reply.getStatus() == ReviewReplyStatus.HIDDEN) {
            throw new BadRequestException("Cannot edit a hidden reply");
        }

        // Get current language
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Update translation for current language
        ReviewReplyTranslation currentTranslation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == currentLanguage)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Translation not found"));

        currentTranslation.setContent(request.content());
        reviewReplyTranslationRepository.save(currentTranslation);

        // Update translation for other language
        Language otherLanguage = (currentLanguage == Language.VI) ? Language.EN : Language.VI;
        String translatedContent = translationService.translate(request.content(), otherLanguage.name());

        ReviewReplyTranslation otherTranslation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == otherLanguage)
                .findFirst()
                .orElseGet(() -> {
                    ReviewReplyTranslation newTranslation = ReviewReplyTranslation.builder()
                            .reviewReply(reply)
                            .language(otherLanguage)
                            .content(translatedContent)
                            .build();
                    reply.getTranslations().add(newTranslation);
                    return newTranslation;
                });

        otherTranslation.setContent(translatedContent);
        reviewReplyTranslationRepository.save(otherTranslation);

        // Mark as edited
        reply.setEdited(true);
        reply.setEditedAt(LocalDateTime.now());
        reviewReplyRepository.save(reply);

        return reviewReplyMapper.mapToReviewReplyResponse(reply, currentLanguage);
    }

    @Override
    @Transactional
    public void deleteReviewReply(Long replyId) {
        // Get current user
        UserResponse currentUser = userService.getCurrentUser();

        // Find reply
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Review reply not found"));

        // Check ownership (user can only delete their own replies, admin can delete any)
        if (!reply.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own replies");
        }

        reviewReplyRepository.delete(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReplyResponse> getReviewRepliesForCustomer(Long reviewId) {
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Get top-level replies
        List<ReviewReply> topLevelReplies = reviewReplyRepository
                .findTopLevelRepliesByReviewIdAndStatus(reviewId, ReviewReplyStatus.APPROVED);

        return topLevelReplies.stream()
                .map(reply -> reviewReplyMapper.mapToReviewReplyResponseWithChildren(reply, currentLanguage))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReplyResponse> getAllReviewRepliesForAdmin(Long reviewId) {
        Language currentLanguage = LanguageUtils.getCurrentLanguage();

        // Validate review exists
        if (!reviewRepository.existsById(reviewId)) {
            throw new NotFoundException("Review not found");
        }

        // Get all top-level replies regardless of status
        List<ReviewReply> topLevelReplies = reviewReplyRepository
                .findAllTopLevelRepliesByReviewId(reviewId);

        return topLevelReplies.stream()
                .map(reply -> reviewReplyMapper.mapToReviewReplyResponseForAdmin(reply, currentLanguage))
                .toList();
    }

    @Override
    @Transactional
    public ReviewReplyResponse moderateReviewReply(Long replyId, ModerateReviewReplyRequest request) {
        // Find reply
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Review reply not found"));

        ReviewReplyStatus currentStatus = reply.getStatus();
        ReviewReplyStatus newStatus = ReviewReplyStatus.valueOf(request.newStatus());

        // Check if status is unchanged
        if (currentStatus == newStatus) {
            return reviewReplyMapper.mapToReviewReplyResponseForAdmin(reply, LanguageUtils.getCurrentLanguage());
        }

        // Validate status transitions
        validateReplyStatusTransition(currentStatus, newStatus);

        // Update status
        reply.setStatus(newStatus);

        // Save updated reply
        reply = reviewReplyRepository.save(reply);

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return reviewReplyMapper.mapToReviewReplyResponse(reply, currentLanguage);
    }

    private void validateReplyStatusTransition(ReviewReplyStatus currentStatus, ReviewReplyStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                // PENDING can go to APPROVED or HIDDEN
                if (newStatus != ReviewReplyStatus.APPROVED && newStatus != ReviewReplyStatus.HIDDEN) {
                    throw new BadRequestException("PENDING reply can only be changed to APPROVED or HIDDEN");
                }
                break;
            case APPROVED:
                // APPROVED can go to HIDDEN or back to PENDING for re-review
                if (newStatus != ReviewReplyStatus.HIDDEN && newStatus != ReviewReplyStatus.PENDING) {
                    throw new BadRequestException("APPROVED reply can only be changed to HIDDEN or PENDING");
                }
                break;
            case HIDDEN:
                // HIDDEN can be restored to APPROVED or PENDING
                if (newStatus != ReviewReplyStatus.APPROVED && newStatus != ReviewReplyStatus.PENDING) {
                    throw new BadRequestException("HIDDEN reply can only be changed to APPROVED or PENDING");
                }
                break;
            default:
                throw new BadRequestException("Unknown review reply status: " + currentStatus);
        }
    }

    // Get product review statistics
    private ProductReviewStatistics getProductReviewStatistics(Long productId, List<ReviewStatus> allowedStatuses) {
        // Get total reviews
        Specification<Review> countSpec = ReviewSpecification.hasProductId(productId)
                .and(ReviewSpecification.hasStatuses(allowedStatuses));
        long totalReviews = reviewRepository.count(countSpec);

        // Get average rating
        Double avgRating = reviewRepository.getAverageRating(productId, allowedStatuses);
        double averageRating = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 0.0;

        // Get rating counts (1-5)
        List<Object[]> ratingData = reviewRepository.countReviewsByRating(productId, allowedStatuses);
        Map<Integer, Long> ratingCounts = new java.util.HashMap<>();

        // Initialize all ratings with 0
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }

        // Fill in actual counts
        for (Object[] row : ratingData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingCounts.put(rating, count);
        }

        // Get reviews with images count
        long reviewsWithImages = reviewRepository.countReviewsWithImages(productId, allowedStatuses);

        // Get reviews with comments count
        long reviewsWithComments = reviewRepository.countReviewsWithComments(productId, allowedStatuses);

        return new ProductReviewStatistics(
                totalReviews,
                averageRating,
                ratingCounts,
                reviewsWithImages,
                reviewsWithComments
        );
    }

    // Auto-moderate review using AI
    private ReviewStatus autoModerateReview(Review review, List<ReviewTranslation> adminCommentTranslations) {
        StringBuilder violationReasons = new StringBuilder();
        boolean hasViolation = false;
        double minConfidenceThreshold = 0.75; // Minimum confidence to consider a violation

        for (ReviewTranslation translation : review.getTranslations()) {
            if (translation.getComment() != null && !translation.getComment().trim().isEmpty()) {
                // Check text comment
                ContentModerationResult textResult = contentModerationService.moderateText(translation.getComment());

                if (textResult.confidenceScore() < 0.5) {
                    // AI is not confident, require manual review
                    return ReviewStatus.NEED_REVIEW;
                }

                // Check for violation with sufficient confidence
                if (textResult.isViolated() && textResult.confidenceScore() >= minConfidenceThreshold) {
                    hasViolation = true;
                    violationReasons.append("Comment (").append(translation.getLanguage())
                            .append("): ").append(textResult.reason()).append(". ");
                    break;
                }
            }
        }

        // Check images if no text violation found
        if (!hasViolation && !review.getImages().isEmpty()) {
            for (ReviewImage image : review.getImages()) {
                // Check image violation
                ContentModerationResult imageResult = contentModerationService.moderateImage(image.getImageUrl());

                if (imageResult.confidenceScore() < 0.5) {
                    return ReviewStatus.NEED_REVIEW;
                }

                if (imageResult.isViolated() && imageResult.confidenceScore() >= minConfidenceThreshold) {
                    hasViolation = true;
                    violationReasons.append("Image violation: ").append(imageResult.reason()).append(". ");
                    break;
                }
            }
        }

        // Result processing
        if (hasViolation) {
            // Create admin comment for both languages
            String violationMessage = "Your review has been automatically rejected due to policy violations: " + violationReasons.toString();
            String violationMessageVi = translationService.translate(violationMessage, "vi");

            // Add admin comment translations
            ReviewTranslation enTranslation = review.getTranslations().stream()
                    .filter(t -> t.getLanguage() == Language.EN)
                    .findFirst()
                    .orElseGet(() -> {
                        ReviewTranslation newTrans = ReviewTranslation.builder()
                                .review(review)
                                .language(Language.EN)
                                .build();
                        review.getTranslations().add(newTrans);
                        return newTrans;
                    });
            enTranslation.setAdminComment(violationMessage);

            ReviewTranslation viTranslation = review.getTranslations().stream()
                    .filter(t -> t.getLanguage() == Language.VI)
                    .findFirst()
                    .orElseGet(() -> {
                        ReviewTranslation newTrans = ReviewTranslation.builder()
                                .review(review)
                                .language(Language.VI)
                                .build();
                        review.getTranslations().add(newTrans);
                        return newTrans;
                    });
            viTranslation.setAdminComment(violationMessageVi != null ? violationMessageVi : violationMessage);

            return ReviewStatus.REJECTED;
        }

        // No violations found, auto-approve
        return ReviewStatus.AUTO_APPROVED;
    }

    // Get review statistics by status for admin dashboard
    private ReviewStatisticsByStatus getReviewStatistics() {
        // Get total reviews
        long totalReviews = reviewRepository.countAllReviews();

        // Get counts by status
        List<Object[]> statusData = reviewRepository.countReviewsByStatus();
        Map<ReviewStatus, Long> statusCounts = new HashMap<>();

        // Initialize all statuses with 0
        for (ReviewStatus status : ReviewStatus.values()) {
            statusCounts.put(status, 0L);
        }

        // Fill in actual counts
        for (Object[] row : statusData) {
            ReviewStatus status = (ReviewStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status, count);
        }

        return new ReviewStatisticsByStatus(totalReviews, statusCounts);
    }

    // Function to validate allowed status transitions
    private void validateStatusTransition(ReviewStatus currentStatus, ReviewStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                // PENDING -> APPROVED, REJECTED, HIDDEN
                if (newStatus != ReviewStatus.APPROVED
                        && newStatus != ReviewStatus.REJECTED
                        && newStatus != ReviewStatus.HIDDEN) {
                    throw new BadRequestException(
                            "PENDING review can only be changed to APPROVED, REJECTED, or HIDDEN");
                }
                break;

            case AUTO_APPROVED:
                // AUTO_APPROVED -> APPROVED, HIDDEN, NEED_REVIEW
                if (newStatus != ReviewStatus.APPROVED
                        && newStatus != ReviewStatus.HIDDEN
                        && newStatus != ReviewStatus.NEED_REVIEW) {
                    throw new BadRequestException(
                            "AUTO_APPROVED review can only be changed to APPROVED, HIDDEN, or NEED_REVIEW");
                }
                break;

            case NEED_REVIEW:
                // NEED_REVIEW -> APPROVED, REJECTED, HIDDEN
                if (newStatus != ReviewStatus.APPROVED
                        && newStatus != ReviewStatus.REJECTED
                        && newStatus != ReviewStatus.HIDDEN) {
                    throw new BadRequestException(
                            "NEED_REVIEW review can only be changed to APPROVED, REJECTED, or HIDDEN");
                }
                break;

            case APPROVED:
                // APPROVED -> HIDDEN (chỉ có thể ẩn)
                if (newStatus != ReviewStatus.HIDDEN) {
                    throw new BadRequestException(
                            "APPROVED review can only be changed to HIDDEN");
                }
                break;

            case REJECTED:
                // REJECTED -> không thể chuyển (final state)
                throw new BadRequestException(
                        "REJECTED review cannot be changed to any other status. This is a final state.");

            case HIDDEN:
                // HIDDEN -> APPROVED (khôi phục)
                if (newStatus != ReviewStatus.APPROVED) {
                    throw new BadRequestException(
                            "HIDDEN review can only be changed to APPROVED (restore)");
                }
                break;

            default:
                throw new BadRequestException("Unknown review status: " + currentStatus);
        }
    }

    // Validate image files for count, size, and type
    private void validateImageFiles(List<MultipartFile> imageFiles) {
        // Validate number of images
        if (imageFiles.size() > 5) {
            throw new BadRequestException("Maximum 5 images allowed");
        }

        long maxSize = 5 * 1024 * 1024L; // 5MB per file
        for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                // Validate file size
                if (file.getSize() > maxSize) {
                    throw new BadRequestException("Each image file must not exceed 5MB");
                }

                String contentType = file.getContentType();
                // Validate file type
                if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
                    throw new BadRequestException("All files must be valid images");
                }
            }
        }
    }
}
