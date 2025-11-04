/*
 * @ {#} ReviewSpecification.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.specifications;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import vn.edu.iuh.fit.entities.Review;
import vn.edu.iuh.fit.entities.ReviewImage;
import vn.edu.iuh.fit.entities.ReviewTranslation;
import vn.edu.iuh.fit.enums.ReviewStatus;

import java.util.List;

/*
 * @description: Specification class for building dynamic queries for Review entities.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
@UtilityClass
public class ReviewSpecification {

    // Function to filter reviews by product ID
    public static Specification<Review> hasProductId(Long productId) {
        return (root, query, criteriaBuilder) -> {
            if (productId == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("productVariant").get("product").get("id"),
                    productId
            );
        };
    }

    // Function to filter reviews by a list of statuses
    public static Specification<Review> hasStatuses(List<ReviewStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return root.get("status").in(statuses);
        };
    }

    // Function to filter reviews by rating
    public static Specification<Review> hasRating(Integer rating) {
        return (root, query, criteriaBuilder) -> {
            if (rating == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("rating"), rating);
        };
    }

    // Function to filter reviews that have images
    public static Specification<Review> hasImages(Boolean hasImages) {
        return (root, query, criteriaBuilder) -> {
            if (hasImages == null || !hasImages) {
                return null;
            }

            // Join with ReviewImage to check if review has images
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ReviewImage> imageRoot = subquery.from(ReviewImage.class);
            subquery.select(criteriaBuilder.count(imageRoot.get("id")))
                    .where(criteriaBuilder.equal(imageRoot.get("review"), root));

            return criteriaBuilder.greaterThan(subquery, 0L);
        };
    }

    // Function to filter reviews that have comments
    public static Specification<Review> hasComment(Boolean hasComment) {
        return (root, query, criteriaBuilder) -> {
            if (hasComment == null || !hasComment) {
                return null;
            }

            // Join with ReviewTranslation to check if review has non-empty comment
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ReviewTranslation> translationRoot = subquery.from(ReviewTranslation.class);
            subquery.select(criteriaBuilder.count(translationRoot.get("id")))
                    .where(
                            criteriaBuilder.equal(translationRoot.get("review"), root),
                            criteriaBuilder.isNotNull(translationRoot.get("comment")),
                            criteriaBuilder.notEqual(translationRoot.get("comment"), "")
                    );

            return criteriaBuilder.greaterThan(subquery, 0L);
        };
    }
}
