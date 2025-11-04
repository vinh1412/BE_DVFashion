/*
 * @ {#} OrderService.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Pageable;
import vn.edu.iuh.fit.dtos.request.AdminUpdateOrderRequest;
import vn.edu.iuh.fit.dtos.request.CancelOrderRequest;
import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.request.UpdateOrderByUserRequest;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;

import java.util.List;

/*
 * @description: Service interface for managing orders
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
public interface OrderService {
    /**
     * Create a new order based on the provided request.
     *
     * @param request the create order request containing order details
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Confirm PayPal payment for an order.
     *
     * @param token       the PayPal payment token
     * @param orderNumber the order number to confirm payment for
     * @return the updated order response after confirming payment
     */
    OrderResponse confirmPayPalPayment(String token, String orderNumber);

    /**
     * Handle PayPal payment cancellation for an order.
     *
     * @param orderNumber the order number for which the payment was cancelled
     * @return a message indicating the cancellation status
     */
    String cancelPayPalPayment(String orderNumber);

    /**
     * Update an order by the user.
     *
     * @param orderNumber the order number to update
     * @param request     the update order request containing updated details
     * @return the updated order response
     */
    OrderResponse updateOrderByUser(String orderNumber, UpdateOrderByUserRequest request);

    /**
     * Update an order by an admin or staff member.
     *
     * @param orderNumber the order number to update
     * @param request     the admin update order request containing updated details
     * @return the updated order response
     */
    OrderResponse adminUpdateOrder(String orderNumber, AdminUpdateOrderRequest request);

    /**
     * Retrieve an order by its order number.
     *
     * @param orderNumber the unique order number
     * @return the order response if found
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Get all orders for the current customer.
     *
     * @return list of order responses for the current customer
     */
    List<OrderResponse> getOrdersByCurrentCustomer();

    /**
     * Get all orders for a specific customer (admin/staff only).
     *
     * @param customerId the customer ID to retrieve orders for
     * @return list of order responses for the specified customer
     */
    List<OrderResponse> getOrdersByCustomerId(Long customerId);

    /**
     * Get all orders for the current customer with pagination.
     *
     * @param pageable pagination information
     * @return page response of order responses for the current customer
     */
    PageResponse<OrderResponse> getOrdersByCurrentCustomerPaging(Pageable pageable);

    /**
     * Get all orders for a specific customer with pagination (admin/staff only).
     *
     * @param customerId the customer ID to retrieve orders for
     * @param pageable pagination information
     * @return page response of order responses for the specified customer
     */
    PageResponse<OrderResponse> getOrdersByCustomerIdPaging(Long customerId, Pageable pageable);

    /**
     * Get all orders with pagination (admin/staff only).
     *
     * @param pageable pagination information
     * @return page response of all order responses
     */
    PageResponse<OrderResponse> getAllOrdersPaging(Pageable pageable);

    /**
     * Get all orders (admin/staff only).
     *
     * @return list of all order responses
     */
    List<OrderResponse> getAllOrders();

    /**
     * Cancel an order by the customer.
     *
     * @param orderNumber the order number to cancel
     * @param request     the cancel order request containing cancellation reason
     * @return the updated order response after cancellation
     */
    OrderResponse cancelOrderByCustomer(String orderNumber, CancelOrderRequest request);
}
