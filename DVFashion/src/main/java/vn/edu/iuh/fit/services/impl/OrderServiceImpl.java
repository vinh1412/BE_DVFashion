/*
 * @ {#} OrderServiceImpl.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.request.OrderItemRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.exceptions.InsufficientStockException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.OrderMapper;
import vn.edu.iuh.fit.mappers.ShippingInfoMapper;
import vn.edu.iuh.fit.repositories.CartItemRepository;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.*;
import vn.edu.iuh.fit.utils.LanguageUtils;
import vn.edu.iuh.fit.utils.OrderUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Implementation of OrderService for managing orders
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final CartItemRepository cartItemRepository;

    private final UserRepository userRepository;

    private final PromotionService promotionService;

    private final InventoryService inventoryService;

    private final UserServiceImpl userService;

    private final OrderItemService orderItemService;

    private final PaymentService paymentService;

    private final OrderMapper orderMapper;

    private final ShippingInfoMapper shippingInfoMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user with {} items", request.orderItems().size());

        // Validate user exists
        UserResponse currentUserResponse = userService.getCurrentUser();
        User customer = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Create order
        Order order = Order.builder()
                .orderNumber(OrderUtils.generateOrderNumber())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .shippingFee(request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO)
                .shippingInfo(shippingInfoMapper.mapToShippingInfo(request.shippingInfo()))
                .notes(request.notes())
                .orderDate(LocalDateTime.now())
                .build();

        // Validate cart items and check inventory
        List<CartItem> cartItems = validateAndReserveStock(request.orderItems(), customer, order);

        // Apply promotion if provided
        if (request.promotionId() != null) {
            Promotion promotion = promotionService.validatePromotion(request.promotionId());
            order.setPromotion(promotion);
        }

        // Create order items
        List<OrderItem> orderItems = orderItemService.createOrderItems(cartItems, order);
        order.setItems(orderItems);

        // Create payment
        Payment payment = paymentService.createPayment(order, PaymentMethod.valueOf(request.paymentMethod()));
        order.setPayment(payment);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear processed cart items
        cartItemRepository.deleteAll(cartItems);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        Language language= LanguageUtils.getCurrentLanguage();
        return orderMapper.mapToOrderResponse(savedOrder, currentUserResponse.getEmail(), language);
    }

    // Validate cart items belong to user and check & reserve stock
    private List<CartItem> validateAndReserveStock(List<OrderItemRequest> orderItems, User customer, Order order) {
        List<CartItem> cartItems = new ArrayList<>();

        for (OrderItemRequest item : orderItems) {
            // Check cart item exists
            CartItem cartItem = cartItemRepository.findById(item.cartItemId())
                    .orElseThrow(() -> new NotFoundException("Cart item not found: " + item.cartItemId()));

            // Validate that cart item belongs to the current user
            if (!cartItem.getCart().getUser().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Cart item " + item.cartItemId() + " does not belong to current user");
            }

            // Check stock availability
            if (!inventoryService.checkAvailability(cartItem.getSize().getId(), cartItem.getQuantity())) {
                throw new InsufficientStockException("Insufficient stock for item: " + cartItem.getProductVariant().getColor());
            }

            // Generate unique reference number for stock reservation(E.g., "ORD-<orderId>-<cartItemId>-<quantity>-<timestamp>")
            String referenceNumber = String.format("ORD-%d-%d-%d-%d", order.getId(), cartItem.getId(), cartItem.getQuantity(), System.currentTimeMillis());

            // Reserve stock
            inventoryService.reserveStock(cartItem.getSize().getId(), cartItem.getQuantity(), referenceNumber);

            cartItems.add(cartItem);
        }

        return cartItems;
    }
}
