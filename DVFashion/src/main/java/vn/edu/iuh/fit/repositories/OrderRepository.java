/*
 * @ {#} OrderRepository.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for order data access
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Find an order by its unique order number.
     *
     * @param orderNumber the unique order number
     * @return an Optional containing the found order, or empty if not found
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders by customer ID, ordered by order date descending.
     *
     * @param customerId the customer ID
     * @return list of orders for the customer
     */
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    /**
     * Find all orders by customer ID with pagination, ordered by order date descending.
     *
     * @param customerId the customer ID
     * @param pageable pagination information
     * @return page of orders for the customer
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Calculates the total revenue from delivered orders within a given date range.
     * Revenue is the sum of the final amount of each payment.
     *
     * @param status the status of the orders to include (e.g., DELIVERED)
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return the total revenue as a BigDecimal, or null if no revenue
     */
    @Query("""
        SELECT SUM(o.payment.amount) 
        FROM Order o 
        WHERE o.status = :status 
            AND o.orderDate 
            BETWEEN :startDate 
            AND :endDate
    """)
    BigDecimal calculateRevenue(@Param("status") OrderStatus status,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Calculates daily revenue within a given date range.
     * @param status The order status.
     * @param startDate The start date.
     * @param endDate The end date.
     * @return A list of objects, where each object array contains the date and the total revenue for that date.
     */
    @Query("""
        SELECT FUNCTION('DATE', o.orderDate), SUM(o.payment.amount)
        FROM Order o
        WHERE o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('DATE', o.orderDate)
        ORDER BY FUNCTION('DATE', o.orderDate)
    """)
    List<Object[]> calculateDailyRevenue(@Param("status") OrderStatus status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Calculates monthly revenue for a given year.
     * @param status The order status.
     * @param year The year.
     * @return A list of objects, where each object array contains the month number and the total revenue for that month.
     */
    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'month', o.orderDate), SUM(o.payment.amount)
        FROM Order o
        WHERE o.status = :status
          AND FUNCTION('DATE_TRUNC', 'year', o.orderDate) = FUNCTION('DATE_TRUNC', 'year', CAST(:year || '-01-01' AS date))
        GROUP BY FUNCTION('DATE_TRUNC', 'month', o.orderDate)
        ORDER BY FUNCTION('DATE_TRUNC', 'month', o.orderDate)
    """)
    List<Object[]> calculateMonthlyRevenue(@Param("status") OrderStatus status,
                                           @Param("year") int year);

    /**
     * Calculates yearly revenue.
     * @param status The order status.
     * @return A list of objects, where each object array contains the year and the total revenue for that year.
     */
    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'year', o.orderDate), SUM(o.payment.amount)
        FROM Order o
        WHERE o.status = :status
        GROUP BY FUNCTION('DATE_TRUNC', 'year', o.orderDate)
        ORDER BY FUNCTION('DATE_TRUNC', 'year', o.orderDate)
    """)
    List<Object[]> calculateYearlyRevenue(@Param("status") OrderStatus status);

    /**
     * Count total number of orders.
     *
     * @return total order count
     */
    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    /**
     * Count number of orders grouped by their status.
     *
     * @return list of object arrays where each array contains status and corresponding count
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Finds best-selling products based on order items for orders with a specific status.
     *
     * @param status   the status of the orders to consider (e.g., DELIVERED)
     * @param language the language for product name translation
     * @return a list of object arrays where each array contains:
     *         - product ID
     *         - product name
     *         - total quantity sold
     *         - total revenue generated
     */
    @Query("""
    SELECT p.id AS productId,
           pt.name AS productName,
           SUM(oi.quantity) AS totalQuantity,
           SUM(oi.quantity * oi.unitPrice) AS totalRevenue
    FROM OrderItem oi
    JOIN oi.order o
    JOIN oi.productVariant pv
    JOIN pv.product p
    JOIN p.translations pt
    WHERE o.status = :status
      AND pt.language = :language
    GROUP BY p.id, pt.name
    ORDER BY totalQuantity DESC
    """)
    List<Object[]> findBestSellingProductsByLanguage(
            @Param("status") OrderStatus status,
            @Param("language") Language language
    );

}
