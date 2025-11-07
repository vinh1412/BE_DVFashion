/*
 * @ {#} ProductSpecification.java   1.0     07/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

        Specification<Product> spec = (root, query, cb) -> null;


        // Search in product name (through translations)
        if (StringUtils.hasText(search)) {
            Specification<Product> searchSpec = (root, query, cb) -> {
                Join<Product, ProductTranslation> translationJoin = root.join("productTranslations", JoinType.LEFT);
                return cb.like(cb.lower(translationJoin.get("name")), "%" + search.toLowerCase() + "%");
            };
            spec = spec.and(searchSpec);
        }

        // Filter by category
        if (categoryId != null) {
            Specification<Product> categorySpec = (root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId);
            spec = spec.and(categorySpec);
        }

        // Filter by promotion
        if (promotionId != null) {
            Specification<Product> promotionSpec = (root, query, cb) -> {
                Join<Product, PromotionProduct> promotionProductJoin = root.join("promotionProducts", JoinType.INNER);
                Join<PromotionProduct, Promotion> promotionJoin = promotionProductJoin.join("promotion", JoinType.INNER);
                return cb.equal(promotionJoin.get("id"), promotionId);
            };
            spec = spec.and(promotionSpec);
        }

        // Filter by status
        if (status != null) {
            Specification<Product> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        // Filter by onSale
        if (onSale != null) {
            Specification<Product> onSaleSpec = (root, query, cb) ->
                    cb.equal(root.get("onSale"), onSale);
            spec = spec.and(onSaleSpec);
        }

        // Filter by price range
        if (minPrice != null) {
            Specification<Product> minPriceSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            spec = spec.and(minPriceSpec);
        }

        if (maxPrice != null) {
            Specification<Product> maxPriceSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            spec = spec.and(maxPriceSpec);
        }

        // Filter by date range
        if (startDate != null) {
            Specification<Product> startDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
            spec = spec.and(startDateSpec);
        }

        if (endDate != null) {
            Specification<Product> endDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59));
            spec = spec.and(endDateSpec);
        }

        return spec;
    }
}
