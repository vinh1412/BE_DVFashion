/*
 * @ {#} ProductSpecification.java   1.0     07/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.ProductTranslation;
import vn.edu.iuh.fit.entities.Promotion;
import vn.edu.iuh.fit.entities.PromotionProduct;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Specification builder for Product entity
 * @author: Tran Hien Vinh
 * @date:   07/11/2025
 * @version:    1.0
 */
@Component
public class ProductSpecification {

    public Specification<Product> build(String search, Long categoryId, Long promotionId,
                                        ProductStatus status, Boolean onSale, BigDecimal minPrice,
                                        BigDecimal maxPrice, LocalDate startDate, LocalDate endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo tên sản phẩm (thông qua bảng translations)
            if (StringUtils.hasText(search)) {
                Join<Product, ProductTranslation> translationJoin = root.join("translations", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(translationJoin.get("name")), "%" + search.toLowerCase() + "%"));
            }

            // Lọc theo danh mục
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // Lọc theo khuyến mãi
            if (promotionId != null) {
                Join<Product, PromotionProduct> promotionProductJoin = root.join("promotionProducts", JoinType.INNER);
                Join<PromotionProduct, Promotion> promotionJoin = promotionProductJoin.join("promotion", JoinType.INNER);
                predicates.add(cb.equal(promotionJoin.get("id"), promotionId));
            }

            // Lọc theo trạng thái
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Lọc theo đang giảm giá
            if (onSale != null) {
                predicates.add(cb.equal(root.get("onSale"), onSale));
            }

            // Lọc theo khoảng giá
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Lọc theo khoảng thời gian
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
            }

            // Thêm DISTINCT để tránh trùng lặp kết quả do JOIN
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
