/*
 * @ {#} OrderRepository.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Order;

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

}
