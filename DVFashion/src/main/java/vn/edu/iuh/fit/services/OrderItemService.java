/*
 * @ {#} OrderItemService.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.entities.CartItem;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Service interface for managing order items
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
public interface OrderItemService {
    /**
     * Create order items from cart items and associate them with the given order.
     *
     * @param cartItems the list of cart items to convert
     * @param order     the order to associate the created order items with
     * @return the list of created order items
     */
    List<OrderItem> createOrderItems(List<CartItem> cartItems, Order order);

    /**
     * Calculate the subtotal amount for a list of order items.
     *
     * @param orderItems the list of order items
     * @return the subtotal amount as BigDecimal
     */
    BigDecimal calculateSubtotal(List<OrderItem> orderItems);
}
