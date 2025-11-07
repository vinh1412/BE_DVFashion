/*
 * @ {#} ReviewMapper.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.ReviewReplyResponse;
import vn.edu.iuh.fit.dtos.response.ReviewResponse;
import vn.edu.iuh.fit.dtos.response.UserSummaryResponse;
import vn.edu.iuh.fit.entities.Review;
import vn.edu.iuh.fit.entities.ReviewImage;
import vn.edu.iuh.fit.entities.ReviewReply;
import vn.edu.iuh.fit.entities.ReviewTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.util.List;
import java.util.Optional;

/*
 * @description: Mapper class for converting between Review entities and DTOs.
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final ReviewReplyMapper reviewReplyMapper;

    public ReviewResponse mapToResponse(Review review) {
        Language responseLanguage = LanguageUtils.getCurrentLanguage();

        String comment = getTranslatedComment(review, responseLanguage);
        String adminComment = getTranslatedAdminComment(review, responseLanguage);
        List<String> imageUrls = getImageUrls(review);
        UserSummaryResponse userSummary = createUserSummary(review);
        String productName = getProductName(review, responseLanguage);
        String variantName = review.getProductVariant().getColor();

        List<ReviewReplyResponse> replies =
                Optional.ofNullable(review.getReplies())
                        .orElseGet(List::of)
                        .stream()
                        .map(reply -> reviewReplyMapper.mapToReviewReplyResponseWithChildren(reply, responseLanguage))
                        .toList();

        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getOrder().getOrderNumber(),
                review.getProductVariant().getId(),
                productName,
                variantName,
                review.getRating(),
                comment,
                review.getStatus(),
                review.getHelpfulCount(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                imageUrls,
                adminComment,
                userSummary,
                replies
        );
    }

    private String getTranslatedComment(Review review, Language language) {
        return review.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .map(ReviewTranslation::getComment)
                .findFirst()
                .orElse(review.getTranslations().stream()
                        .findFirst()
                        .map(ReviewTranslation::getComment)
                        .orElse(null));
    }

    private String getTranslatedAdminComment(Review review, Language language) {
        return review.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .map(t -> Optional.ofNullable(t.getAdminComment()).orElse(""))
                .findFirst()
                .orElse("");
    }

    private List<String> getImageUrls(Review review) {
        return review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();
    }

    private UserSummaryResponse createUserSummary(Review review) {
        return new UserSummaryResponse(
                review.getUser().getId(),
                review.getUser().getFullName()
        );
    }

    private String getProductName(Review review, Language language) {
        return review.getProductVariant().getProduct().getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .map(t -> t.getName())
                .findFirst()
                .orElse(review.getProductVariant().getProduct().getTranslations().stream()
                        .findFirst()
                        .map(t -> t.getName())
                        .orElse(""));
    }
}
