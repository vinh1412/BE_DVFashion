/*
 * @ {#} ReviewReplyMapper.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.ReviewReplyResponse;
import vn.edu.iuh.fit.entities.ReviewReply;
import vn.edu.iuh.fit.entities.ReviewReplyTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ReviewReplyStatus;
import vn.edu.iuh.fit.repositories.ReviewReplyRepository;

import java.util.List;

/*
 * @description: Mapper class for converting between ReviewReply entities and DTOs.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class ReviewReplyMapper {
    private final ReviewReplyRepository reviewReplyRepository;

    public ReviewReplyResponse mapToReviewReplyResponse(ReviewReply reply, Language language) {
        // Get translation for the specified language
        ReviewReplyTranslation translation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> reply.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.VI)
                        .findFirst()
                        .orElse(reply.getTranslations().get(0))
                );

        return new ReviewReplyResponse(
                reply.getId(),
                reply.getReview().getId(),
                reply.getParentReply() != null ? reply.getParentReply().getId() : null,
                reply.getUser().getId(),
                reply.getUser().getFullName(),
                translation.getContent(),
                reply.getStatus(),
                reply.isEdited(),
                reply.getCreatedAt(),
                reply.getEditedAt(),
                List.of() // Empty for simple mapping
        );
    }

    public ReviewReplyResponse mapToReviewReplyResponseWithChildren(ReviewReply reply, Language language) {
        // Get translation for the specified language
        ReviewReplyTranslation translation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> reply.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.VI)
                        .findFirst()
                        .orElse(reply.getTranslations().get(0))
                );

        // Get child replies
        List<ReviewReply> childReplies = reviewReplyRepository
                .findChildRepliesByParentId(reply.getId(), ReviewReplyStatus.APPROVED);

        List<ReviewReplyResponse> childReplyResponses = childReplies.stream()
                .map(childReply -> mapToReviewReplyResponse(childReply, language))
                .toList();

        return new ReviewReplyResponse(
                reply.getId(),
                reply.getReview().getId(),
                reply.getParentReply() != null ? reply.getParentReply().getId() : null,
                reply.getUser().getId(),
                reply.getUser().getFullName(),
                translation.getContent(),
                reply.getStatus(),
                reply.isEdited(),
                reply.getCreatedAt(),
                reply.getEditedAt(),
                childReplyResponses
        );
    }

    public ReviewReplyResponse mapToReviewReplyResponseForAdmin(ReviewReply reply, Language language) {
        ReviewReplyTranslation translation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> reply.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.VI)
                        .findFirst()
                        .orElse(reply.getTranslations().isEmpty() ? null : reply.getTranslations().get(0))
                );

        // Lấy tất cả child replies không phân biệt status
        List<ReviewReply> childReplies = reviewReplyRepository
                .findAllChildRepliesByParentId(reply.getId());

        List<ReviewReplyResponse> childReplyResponses = childReplies.stream()
                .map(childReply -> mapToReviewReplyResponseForAdmin(childReply, language))
                .toList();

        return new ReviewReplyResponse(
                reply.getId(),
                reply.getReview().getId(),
                reply.getParentReply() != null ? reply.getParentReply().getId() : null,
                reply.getUser().getId(),
                reply.getUser().getFullName(),
                translation != null ? translation.getContent() : null,
                reply.getStatus(),
                reply.isEdited(),
                reply.getCreatedAt(),
                reply.getEditedAt(),
                childReplyResponses
        );
    }

    public ReviewReplyResponse mapToReviewReplyResponseWithAllChildren(ReviewReply reply, Language language) {
        ReviewReplyTranslation translation = reply.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> reply.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.VI)
                        .findFirst()
                        .orElse(reply.getTranslations().isEmpty() ? null : reply.getTranslations().get(0))
                );

        // 👉 Lấy tất cả child (bỏ điều kiện status)
        List<ReviewReply> childReplies = reviewReplyRepository
                .findTopLevelRepliesByReviewIdAdmin(reply.getId());

        List<ReviewReplyResponse> childReplyResponses = childReplies.stream()
                .map(childReply -> mapToReviewReplyResponseWithAllChildren(childReply, language))
                .toList();

        return new ReviewReplyResponse(
                reply.getId(),
                reply.getReview().getId(),
                reply.getParentReply() != null ? reply.getParentReply().getId() : null,
                reply.getUser().getId(),
                reply.getUser().getFullName(),
                translation != null ? translation.getContent() : null,
                reply.getStatus(),
                reply.isEdited(),
                reply.getCreatedAt(),
                reply.getEditedAt(),
                childReplyResponses
        );
    }
}
