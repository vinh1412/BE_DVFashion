/*
 * @ {#} OrderSpecification.java   1.0     19/11/2025
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
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.Payment;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: 
 * @author: Tran Hien Vinh
 * @date:   19/11/2025
 * @version:    1.0
 */
@Component
public class OrderSpecification {

    public Specification<Order> build(
            String search,
            OrderStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            Long customerId,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by keywords
            if (StringUtils.hasText(search)) {
                String[] words = search.toLowerCase().trim().split("\\s+");
                List<Predicate> allWordAndPredicates = new ArrayList<>();

                for (String word : words) {
                    String keyword = "%" + word + "%";
                    List<Predicate> perWordOrPreds = new ArrayList<>();

                    // Order Number
                    perWordOrPreds.add(cb.like(cb.lower(root.get("orderNumber")), keyword));

                    // Customer Name
                    Join<Order, User> customerJoin = root.join("customer", JoinType.LEFT);
                    perWordOrPreds.add(cb.like(cb.lower(customerJoin.get("fullName")), keyword));
                    perWordOrPreds.add(cb.like(cb.lower(customerJoin.get("email")), keyword));
                    perWordOrPreds.add(cb.like(cb.lower(customerJoin.get("phone")), keyword));

                    // Shipping Info
                    perWordOrPreds.add(cb.like(cb.lower(root.get("shippingInfo").get("fullName")), keyword));
                    perWordOrPreds.add(cb.like(cb.lower(root.get("shippingInfo").get("phone")), keyword));

                    allWordAndPredicates.add(cb.or(perWordOrPreds.toArray(new Predicate[0])));
                }

                predicates.add(cb.and(allWordAndPredicates.toArray(new Predicate[0])));
            }

            // Filter by ORDER STATUS
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by PAYMENT METHOD
            if (paymentMethod != null) {
                Join<Order, Payment> paymentJoin = root.join("payment", JoinType.LEFT);
                predicates.add(cb.equal(paymentJoin.get("paymentMethod"), paymentMethod));
            }

            // Filter by PAYMENT STATUS
            if (paymentStatus != null) {
                Join<Order, Payment> paymentJoin = root.join("payment", JoinType.LEFT);
                predicates.add(cb.equal(paymentJoin.get("paymentStatus"), paymentStatus));
            }

            // Filter by CUSTOMER ID
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }

            // Filter by TOTAL AMOUNT
            if (minTotal != null || maxTotal != null) {
                // Calculate total = sum of orderItems + shippingFee - voucherDiscount
                // This is simplified, you may need to adjust based on your calculation logic
                if (minTotal != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("payment").get("amount"), minTotal));
                }
                if (maxTotal != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("payment").get("amount"), maxTotal));
                }
            }

            // Filter by ORDER DATE
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
