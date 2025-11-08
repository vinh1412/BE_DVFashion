/*
 * @ {#} OrderItemRepository.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.OrderItem;
import vn.edu.iuh.fit.entities.OrderItemId;

import java.util.List;

/*
 * @description: Repository for managing order items
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    /**
     * Sums the quantity of a specific product ordered by a user in active promotions.
     *
     * @param userId    the ID of the user
     * @param productId the ID of the product
     * @return the total quantity of the product ordered by the user in active promotions
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.productVariant pv " +
            "JOIN PromotionProduct pp ON pp.product.id = pv.product.id " +
            "WHERE o.customer.id = :userId " +
            "AND pv.product.id = :productId " +
            "AND pp.active = true " +
            "AND pp.promotion.active = true " +
            "AND pp.promotion.startDate <= CURRENT_TIMESTAMP " +
            "AND pp.promotion.endDate >= CURRENT_TIMESTAMP " +
            "AND o.status IN (vn.edu.iuh.fit.enums.OrderStatus.PENDING, " +
            "vn.edu.iuh.fit.enums.OrderStatus.CONFIRMED, " +
            "vn.edu.iuh.fit.enums.OrderStatus.PROCESSING, " +
            "vn.edu.iuh.fit.enums.OrderStatus.SHIPPED, " +
            "vn.edu.iuh.fit.enums.OrderStatus.DELIVERED)")
    int sumQuantityByUserAndProductInActivePromotion(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("""
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM OrderItem oi
    JOIN Order o ON oi.order.id = o.id
    JOIN Payment p ON o.id = p.order.id
    WHERE o.customer.id = :userId
      AND oi.productVariant.product.id = :productId
      AND o.status IN :validStatuses
      AND p.paymentStatus = 'COMPLETED'
""")
    int countUserPurchasedQuantityForPromotionProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("validStatuses") List<String> validStatuses);

}
