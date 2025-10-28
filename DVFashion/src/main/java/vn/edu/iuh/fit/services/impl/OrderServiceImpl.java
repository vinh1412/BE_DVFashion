/*
 * @ {#} OrderServiceImpl.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.InsufficientStockException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.OrderException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
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
import java.util.*;

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

    private final PayPalService payPalService;

    private final UserInteractionService userInteractionService;

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
        List<CartItem> cartItems = validateReserveStock(request.orderItems(), customer);

        // Apply promotion if provided
        if (request.promotionId() != null) {
            Promotion promotion = promotionService.validatePromotion(request.promotionId());
            order.setPromotion(promotion);
        }

        // Create order items
        List<OrderItem> orderItems = orderItemService.createOrderItems(cartItems, order);
        order.setItems(orderItems);

        // Create payment
        PaymentMethod method = PaymentMethod.valueOf(request.paymentMethod());
        Payment payment = paymentService.createPayment(order, method);
        order.setPayment(payment);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Track PURCHASE interactions for each item
        try {
            for (OrderItem item : savedOrder.getItems()) {
                userInteractionService.trackInteraction(
                        customer.getId(),
                        item.getProductVariant().getProduct().getId(),
                        InteractionType.PURCHASE,
                        null
                );
            }
        } catch (Exception e) {
            log.warn("Could not track PURCHASE interactions: {}", e.getMessage());
        }

        // Clear processed cart items
        cartItemRepository.deleteAll(cartItems);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // For Cash on Delivery, return order details directly
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return orderMapper.mapToOrderResponse(savedOrder, currentUserResponse.getEmail(), LanguageUtils.getCurrentLanguage());
        }

        // For PayPal, create PayPal order and get approval URL
        if (method == PaymentMethod.PAYPAL) {
            return OrderResponse.builder()
                    .orderNumber(savedOrder.getOrderNumber())
                    .paypalApprovalUrl(payment.getApprovalUrl())
                    .build();
        }

        throw new UnsupportedOperationException("Unsupported payment method: " + method);
    }

    @Override
    public OrderResponse confirmPayPalPayment(String token, String orderNumber) {
        // Capture payment
        payPalService.capturePayment(token);

        // Get order by orderNumber
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Update order status
        order.setStatus(OrderStatus.CONFIRMED);

        // Update payment status
        Payment payment = order.getPayment();
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCapturedAt(LocalDateTime.now());

        // Save order
        orderRepository.save(order);

        // Confirmation of deduction of physical goods in stock
        order.getItems().forEach(item ->
                inventoryService.confirmReservedStock(
                        item.getSize().getId(),
                        item.getQuantity(),
                        "ORD-" + order.getOrderNumber() + "-" + item.getId(),
                        order.getCustomer(),
                        order
                )
        );

        // Map to response
        return orderMapper.mapToOrderResponse(
                order,
                order.getCustomer().getEmail(),
                LanguageUtils.getCurrentLanguage()
        );
    }

    @Transactional
    @Override
    public String cancelPayPalPayment(String orderNumber) {
        // Get order by orderNumber
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Update order status
        order.setStatus(OrderStatus.CANCELED);

        // Update payment status
        Payment payment = order.getPayment();
        if (payment != null) {
            payment.setPaymentStatus(PaymentStatus.CANCELED);
            payment.setPaymentDate(LocalDateTime.now());
        }

        orderRepository.save(order);

        return "Payment cancelled successfully.";
    }

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_STATUS_FLOW = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED_STATUS_FLOW.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED));
        ALLOWED_STATUS_FLOW.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELED));
        ALLOWED_STATUS_FLOW.put(OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED));
        ALLOWED_STATUS_FLOW.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNED));
        ALLOWED_STATUS_FLOW.put(OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED));
        ALLOWED_STATUS_FLOW.put(OrderStatus.CANCELED, Set.of());
        ALLOWED_STATUS_FLOW.put(OrderStatus.RETURNED, Set.of());
    }

    @Override
    public OrderResponse updateOrderByUser(String orderNumber, UpdateOrderByUserRequest request) {
        User current = userRepository.findById(userService.getCurrentUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Ownership check
        if (!order.getCustomer().getId().equals(current.getId())) {
            throw new UnauthorizedException("You are not allowed to update this order");
        }

        // Update shipping info only when PENDING or PROCESSING status
        if (request.fullName() != null || request.phone() != null || request.country() != null
                || request.city() != null || request.ward() != null || request.district() != null
                || request.street() != null) {
            orderMapper.ensureShippingUpdatable(order.getStatus());
            orderMapper.applyShippingInfo(order, request, null);
        }

        // Update notes
        if (request.notes() != null) {
            order.setNotes(request.notes());
        }

        orderRepository.save(order);

        return orderMapper.mapToOrderResponse(
                order,
                order.getCustomer().getEmail(),
                LanguageUtils.getCurrentLanguage()
        );
    }

    @Override
    public OrderResponse adminUpdateOrder(String orderNumber, AdminUpdateOrderRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (request.fullName() != null || request.phone() != null || request.country() != null
                || request.city() != null || request.ward() != null || request.district() != null
                || request.street() != null) {
            orderMapper.ensureShippingUpdatable(order.getStatus());
            orderMapper.applyShippingInfo(order, null, request);
        }

        // Update notes
        if (request.notes() != null) {
            order.setNotes(request.notes());
        }

        // Update order status transition
        if (request.orderStatus() != null) {
            applyOrderStatusTransition(order, OrderStatus.valueOf(request.orderStatus()));
        }

        // Update payment status transition
        if (request.paymentStatus() != null) {
            applyPaymentStatusTransition(order, PaymentStatus.valueOf(request.paymentStatus()));
        }

        orderRepository.save(order);

        return orderMapper.mapToOrderResponse(
                order,
                order.getCustomer().getEmail(),
                LanguageUtils.getCurrentLanguage()
        );
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found with order number: " + orderNumber));

        // Get current user to check ownership (for customers) or allow all access (for admin/staff)
        UserResponse currentUser = userService.getCurrentUser();

        // If user is customer, check ownership
        if (currentUser.getRoles().contains("ROLE_CUSTOMER")  && !Objects.equals(order.getCustomer().getId(), currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to view this order");
        }

        return orderMapper.mapToOrderResponse(
                order,
                currentUser.getEmail(),
                LanguageUtils.getCurrentLanguage()
        );
    }

    @Override
    public List<OrderResponse> getOrdersByCurrentCustomer() {
        UserResponse currentUser = userService.getCurrentUser();

        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(currentUser.getId());

        return orders.stream()
                .map(order -> orderMapper.mapToOrderResponse(
                        order,
                        currentUser.getEmail(),
                        LanguageUtils.getCurrentLanguage()
                ))
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        // Verify customer exists
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);

        return orders.stream()
                .map(order -> orderMapper.mapToOrderResponse(
                        order,
                        customer.getEmail(),
                        LanguageUtils.getCurrentLanguage()
                ))
                .toList();
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByCurrentCustomerPaging(Pageable pageable) {
        UserResponse currentUser = userService.getCurrentUser();

        Page<Order> orders = orderRepository.findByCustomerId(currentUser.getId(), pageable);

        Page<OrderResponse> orderResponsePage = orders.map(order -> orderMapper.mapToOrderResponse(
                order,
                currentUser.getEmail(),
                LanguageUtils.getCurrentLanguage()
        ));

        return PageResponse.from(orderResponsePage);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByCustomerIdPaging(Long customerId, Pageable pageable) {
        // Verify customer exists
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);

        Page<OrderResponse> orderResponsePage = orders.map(order -> orderMapper.mapToOrderResponse(
                order,
                customer.getEmail(),
                LanguageUtils.getCurrentLanguage()
        ));

        return PageResponse.from(orderResponsePage);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrdersPaging(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);

        Page<OrderResponse> orderResponsePage = orders.map(order -> orderMapper.mapToOrderResponse(
                order,
                order.getCustomer().getEmail(),
                LanguageUtils.getCurrentLanguage()
        ));

        return PageResponse.from(orderResponsePage);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(order -> orderMapper.mapToOrderResponse(
                        order,
                        order.getCustomer().getEmail(),
                        LanguageUtils.getCurrentLanguage()
                ))
                .toList();
    }

    // Validate cart items belong to user and check reserve stock
    private List<CartItem> validateReserveStock(List<OrderItemRequest> orderItems, User customer) {
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

            cartItems.add(cartItem);
        }

        return cartItems;
    }

    // Apply order status transition with validation
    private void applyOrderStatusTransition(Order order, OrderStatus target) {
        // Get current status
        OrderStatus current = order.getStatus();

        // No-op if same status
        if (current == target) return;

        // Validate allowed transitions
        Set<OrderStatus> next = ALLOWED_STATUS_FLOW.getOrDefault(current, Set.of());
        if (!next.contains(target)) {
            throw new OrderException("Invalid order status transition: " + current + " -> " + target);
        }

        order.setStatus(target);

        if (target == OrderStatus.CONFIRMED) {
            // Confirmation of deduction of physical goods in stock
            order.getItems().forEach(item ->
                    inventoryService.confirmReservedStock(
                            item.getSize().getId(),
                            item.getQuantity(),
                            "ORD-" + order.getOrderNumber() + "-" + item.getId(),
                            order.getCustomer(),
                            order
                    )
            );
        }

        // When the order is CANCELED, if payment is still PENDING, mark it CANCELED too
        if (target == OrderStatus.CANCELED) {
            // If payment exists and is still pending, mark it canceled
            Payment p = order.getPayment();
            if (p != null && p.getPaymentStatus() == PaymentStatus.PENDING) {
                p.setPaymentStatus(PaymentStatus.CANCELED);
                p.setPaymentDate(LocalDateTime.now());
            }
        }

        // When the order is RETURNED, process stock return to inventory
        if (target == OrderStatus.RETURNED) {
            // Process stock return
           inventoryService.processReturnStock(order);
        }
    }

    // Apply payment status transition with validation
    private void applyPaymentStatusTransition(Order order, PaymentStatus target) {
        Payment payment = order.getPayment();
        if (payment == null) throw new OrderException("Payment not found for this order");

        PaymentStatus current = payment.getPaymentStatus();
        if (current == target) return;

        // Only forward transitions allowed from PENDING
        if (current == PaymentStatus.PENDING) {
            if (target == PaymentStatus.COMPLETED) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());

                // If payment completed and order was PENDING, move to CONFIRMED automatically (optional)
                if (order.getStatus() == OrderStatus.PENDING) {
                    applyOrderStatusTransition(order, OrderStatus.CONFIRMED);
                }
            } else if (target == PaymentStatus.CANCELED) {
                payment.setPaymentStatus(PaymentStatus.CANCELED);
                payment.setPaymentDate(LocalDateTime.now());
                // Optionally cancel order
                if (ALLOWED_STATUS_FLOW.get(order.getStatus()).contains(OrderStatus.CANCELED)) {
                    order.setStatus(OrderStatus.CANCELED);
                }
            } else {
                throw new OrderException("Unsupported payment status transition from PENDING to " + target);
            }
        } else {
            throw new OrderException("Payment status is terminal and cannot be changed");
        }
    }
}
