/*
 * @ {#} UserProductInteractionSpecification.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Specification class for building dynamic queries for UserProductInteraction entities.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
public class UserProductInteractionSpecification {
    public static Specification<UserProductInteraction> filterBy(
            Long userId,
            Long productId,
            InteractionType interactionType,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), productId));
            }

            if (interactionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("interactionType"), interactionType));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
