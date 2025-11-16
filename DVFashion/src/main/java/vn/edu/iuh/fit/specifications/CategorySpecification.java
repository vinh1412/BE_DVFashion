/*
 * @ {#} CategorySpecification.java   1.0     15/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.specifications;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.entities.CategoryTranslation;
import vn.edu.iuh.fit.entities.Product;

import java.util.ArrayList;
import java.util.List;

/*
 * @description: Specification builder for Category entity
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
@Component
public class CategorySpecification {

    public Specification<Category> build(
            String search,
            Boolean active,
            Boolean hasProducts
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // JOIN translations để search
            Join<Category, CategoryTranslation> trJoin =
                    root.join("translations", JoinType.LEFT);

            // SEARCH (keyword tách từng từ)
            if (StringUtils.hasText(search)) {

                String[] words = search.toLowerCase().trim().split("\\s+");

                List<Predicate> allWordAndPredicates = new ArrayList<>();

                for (String word : words) {
                    String keyword = "%" + word + "%";

                    List<Predicate> perWordOrPreds = new ArrayList<>();

                    perWordOrPreds.add(cb.like(cb.lower(trJoin.get("name")), keyword));
                    perWordOrPreds.add(cb.like(cb.lower(trJoin.get("description")), keyword));

                    allWordAndPredicates.add(
                            cb.or(perWordOrPreds.toArray(new Predicate[0]))
                    );
                }

                predicates.add(
                        cb.and(allWordAndPredicates.toArray(new Predicate[0]))
                );
            }

            // FILTER ACTIVE
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            // FILTER CÓ SẢN PHẨM
            if (hasProducts != null) {

                Subquery<Long> sub = query.subquery(Long.class);
                Root<Product> subRoot = sub.from(Product.class);

                sub.select(subRoot.get("category").get("id"))
                        .where(cb.equal(subRoot.get("category"), root));

                if (hasProducts) {
                    predicates.add(cb.exists(sub));
                } else {
                    predicates.add(cb.not(cb.exists(sub)));
                }
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

