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
import vn.edu.iuh.fit.entities.embedded.ShippingInfo;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.InsufficientStockException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.OrderException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.mappers.OrderMapper;
import vn.edu.iuh.fit.mappers.ShippingInfoMapper;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.services.*;
import vn.edu.iuh.fit.utils.LanguageUtils;
import vn.edu.iuh.fit.utils.OrderUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    private final EmailService emailService;

    private final ShippingService shippingService;

    private final VoucherService voucherService;

    private final StockTransactionRepository stockTransactionRepository;

    private final InventoryRepository inventoryRepository;


    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user with {} items", request.orderItems().size());

        // Validate user exists
        UserResponse currentUserResponse = userService.getCurrentUser();
        User customer = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Calculate shipping fee and delivery time
        ShippingCalculationResponse shippingCalculation = shippingService.calculateShipping(request);

        // Create order
        Order order = Order.builder()
                .orderNumber(OrderUtils.generateOrderNumber())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .shippingFee(shippingCalculation.shippingFee())
                .estimatedDeliveryTime(shippingCalculation.estimatedDeliveryTime())
                .shippingInfo(shippingInfoMapper.mapToShippingInfo(request.shippingInfo()))
                .notes(request.notes())
                .orderDate(LocalDateTime.now())
                .build();

        // Validate cart items and check inventory
        List<CartItem> cartItems = validateReserveStock(request.orderItems(), customer);

        // Create order items
        List<OrderItem> orderItems = orderItemService.createOrderItems(cartItems, order);
        order.setItems(orderItems);

        // Create payment
        PaymentMethod method = PaymentMethod.valueOf(request.paymentMethod());
        Payment payment = paymentService.createPayment(order, method);
        order.setPayment(payment);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Apply voucher if provided
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            // Validate and apply voucher
            Voucher voucher = voucherService.validateAndApplyVoucher(request.voucherCode(), customer, savedOrder);

            savedOrder.setVoucher(voucher);
            savedOrder.setVoucherCode(voucher.getCode());

            // Calculate voucher discount
            BigDecimal subtotal = orderItemService.calculateSubtotal(savedOrder.getItems());

            // Calculate voucher discount
            BigDecimal voucherDiscount = voucherService.calculateVoucherDiscount(voucher, subtotal, savedOrder.getItems());

            savedOrder.setVoucherDiscount(voucherDiscount);

            log.info("Voucher {} applied to order {}", voucher.getCode(), savedOrder.getOrderNumber());

            // Update payment amount
            if (savedOrder.getPayment() != null) {
                BigDecimal total = orderMapper.calculateOrderTotal(savedOrder);
                savedOrder.getPayment().setAmount(total);
            }

            log.info("Voucher {} applied. Subtotal={}, Discount={}, Final total={}",
                    voucher.getCode(), subtotal, voucherDiscount, orderMapper.calculateOrderTotal(savedOrder));

            // Record voucher usage
            voucherService.recordUsage(voucher, customer, savedOrder);
        }

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Create order response
        OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                savedOrder,
                currentUserResponse.getEmail(),
                LanguageUtils.getCurrentLanguage()
        );

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // For Cash on Delivery, return order details directly
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            // Clear cart items only for COD
            cartItemRepository.deleteAll(cartItems);

            // Track interactions
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

            // Send order confirmation email (async to avoid blocking)
            try {
                log.info("Sending order confirmation email to {}", customer.getEmail());
                emailService.sendOrderConfirmationEmail(orderResponse, customer.getEmail());
            } catch (Exception e) {
                log.error("Failed to send order confirmation email: {}", e.getMessage());
            }

            return orderMapper.mapToOrderResponse(savedOrder, currentUserResponse.getEmail(), LanguageUtils.getCurrentLanguage());
        }

        // For PayPal, create PayPal order and get approval URL
        if (method == PaymentMethod.PAYPAL) {
            log.info("PayPal order created, email will be sent after payment confirmation");
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
        String captureId = payPalService.capturePayment(token);

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
        payment.setPaypalCaptureId(captureId);

        // Save order
        orderRepository.save(order);

        // NOW clear cart items after successful payment
        List<CartItem> cartItems = cartItemRepository.findByCartUserId(order.getCustomer().getId());
        List<CartItem> orderCartItems = cartItems.stream()
                .filter(cartItem -> order.getItems().stream()
                        .anyMatch(orderItem ->
                                orderItem.getSize().getId().equals(cartItem.getSize().getId()) &&
                                        orderItem.getProductVariant().getId().equals(cartItem.getProductVariant().getId())
                        ))
                .collect(Collectors.toList());

        cartItemRepository.deleteAll(orderCartItems);

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

        // Track purchase interactions
        try {
            for (OrderItem item : order.getItems()) {
                userInteractionService.trackInteraction(
                        order.getCustomer().getId(),
                        item.getProductVariant().getProduct().getId(),
                        InteractionType.PURCHASE,
                        null
                );
            }
        } catch (Exception e) {
            log.warn("Could not track PURCHASE interactions: {}", e.getMessage());
        }

        // Map to response
        OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                order,
                order.getCustomer().getEmail(),
                LanguageUtils.getCurrentLanguage()
        );

        // Send order confirmation email AFTER successful PayPal payment
        try {
            log.info("Sending order confirmation email to {} for successful PayPal payment", order.getCustomer().getEmail());
            emailService.sendOrderConfirmationEmail(orderResponse, order.getCustomer().getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for PayPal payment: {}", e.getMessage());
        }

        return orderResponse;
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

        if (hasShippingInfoChanged(order, request)) {
            orderMapper.ensureShippingUpdatable(order.getStatus());
            orderMapper.applyShippingInfo(order, null, request);
        }

        // Update notes
        if (request.notes() != null) {
            order.setNotes(request.notes());
        }

        // Update order status transition
        if (request.orderStatus() != null) {
            OrderStatus targetStatus = OrderStatus.valueOf(request.orderStatus());

            // Special handling for admin cancellation
            if (targetStatus == OrderStatus.CANCELED && order.getStatus() != OrderStatus.CANCELED) {
                processAdminCancellation(order, request.cancellationReason());
            } else {
                applyOrderStatusTransition(order, targetStatus);
            }
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

    private void processAdminCancellation(Order order, String cancellationReason) {
        OrderStatus oldStatus = order.getStatus();

        // Update order status
        order.setStatus(OrderStatus.CANCELED);

        // Add cancellation reason to notes
        String existingNotes = order.getNotes() != null ? order.getNotes() : "";
        String cancellationNote = "CANCELLED BY ADMIN" +
                (cancellationReason != null ? ": " + cancellationReason : "");

        if (!existingNotes.isEmpty()) {
            order.setNotes(existingNotes + " | " + cancellationNote);
        } else {
            order.setNotes(cancellationNote);
        }

        // Process payment cancellation (same as customer cancellation)
        processPaymentCancellation(order);

        // Process stock restoration (same as customer cancellation)
        processStockRestoration(order, oldStatus);

        // Send admin cancellation email
        try {
            OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                    order,
                    order.getCustomer().getEmail(),
                    LanguageUtils.getCurrentLanguage()
            );

            emailService.sendOrderCancellationEmail(
                    orderResponse,
                    order.getCustomer().getEmail(),
                    cancellationNote
            );
        } catch (Exception e) {
            log.error("Failed to send admin order cancellation email: {}", e.getMessage());
        }

        log.info("Order {} cancelled by admin", order.getOrderNumber());
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

    @Override
    @Transactional
    public OrderResponse cancelOrderByCustomer(String orderNumber, CancelOrderRequest request) {
            // Get current user
            UserResponse currentUser = userService.getCurrentUser();

            // Find order
            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            // Check ownership
            if (!order.getCustomer().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You can only cancel your own orders");
            }

            // Validate cancellation eligibility
            validateCancellationEligibility(order);

            // Process cancellation based on payment method
            processCancellation(order, request.cancellationReason());

            // Save updated order
            orderRepository.save(order);

            return orderMapper.mapToOrderResponse(
                    order,
                    currentUser.getEmail(),
                    LanguageUtils.getCurrentLanguage()
            );
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics() {
        long totalOrders = orderRepository.countTotalOrders();

        // Get counts order by status
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();

        Map<OrderStatus, Long> ordersByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> (OrderStatus) row[0],
                        row -> (Long) row[1]
                ));

        return OrderStatisticsResponse.builder()
                .totalOrders(totalOrders)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    private void validateCancellationEligibility(Order order) {
        // Check status - only PENDING or CONFIRMED can be cancelled
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        // Check time limit - must be within 24 hours
        LocalDateTime orderDate = order.getOrderDate();
        LocalDateTime cutoffTime = orderDate.plusHours(24);

        if (LocalDateTime.now().isAfter(cutoffTime)) {
            throw new OrderException("Order can only be cancelled within 24 hours of placement");
        }

        log.info("Order {} is eligible for cancellation", order.getOrderNumber());
    }

    private void processCancellation(Order order, String cancellationReason) {
        OrderStatus oldStatus = order.getStatus();

        // Update order status and add cancellation reason
        order.setStatus(OrderStatus.CANCELED);

        // Add cancellation reason to notes
        String existingNotes = order.getNotes() != null ? order.getNotes() : "";
        String cancellationNote = "CANCELLED BY CUSTOMER: " + cancellationReason;

        if (!existingNotes.isEmpty()) {
            order.setNotes(existingNotes + " | " + cancellationNote);
        } else {
            order.setNotes(cancellationNote);
        }

        // Process payment cancellation
        processPaymentCancellation(order);

        // Process stock restoration
        processStockRestoration(order, oldStatus);

        // Send cancellation email
        try {
            OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                    order,
                    order.getCustomer().getEmail(),
                    LanguageUtils.getCurrentLanguage()
            );

            emailService.sendOrderCancellationEmail(
                    orderResponse,
                    order.getCustomer().getEmail(),
                    cancellationReason
            );
        } catch (Exception e) {
            log.error("Failed to send order cancellation email: {}", e.getMessage());
        }

        log.info("Order {} cancelled successfully by customer", order.getOrderNumber());
    }

    private void processPaymentCancellation(Order order) {
        // Get payment details
        Payment payment = order.getPayment();
        if (payment == null) return;

        switch (payment.getPaymentMethod()) {
            case CASH_ON_DELIVERY -> {
                // For COD, just mark payment as cancelled - no refund needed
                payment.setPaymentStatus(PaymentStatus.CANCELED);
                payment.setPaymentDate(LocalDateTime.now());
                log.info("COD payment cancelled for order {}", order.getOrderNumber());
            }
            case PAYPAL -> {
                // For PayPal, process refund if payment was completed
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    try {
                        // Process PayPal refund
                        payPalService.refundPayment(payment.getPaypalCaptureId(), payment.getAmount());
                        payment.setPaymentStatus(PaymentStatus.REFUNDED);
                        log.info("PayPal refund processed for order {}", order.getOrderNumber());
                    } catch (Exception e) {
                        log.error("Failed to process PayPal refund for order {}: {}",
                                order.getOrderNumber(), e.getMessage());
                        throw new OrderException("Failed to process refund. Please contact support.");
                    }
                } else {
                    // If payment was still pending, just cancel it
                    payment.setPaymentStatus(PaymentStatus.CANCELED);
                    payment.setPaymentDate(LocalDateTime.now());
                    log.info("Pending PayPal payment cancelled for order {}", order.getOrderNumber());
                }
            }
            default -> throw new OrderException("Unsupported payment method for cancellation");
        }
    }

    private void processStockRestoration(Order order, OrderStatus oldStatus) {
        // Get current user for audit
        User currentUser = userRepository.findById(userService.getCurrentUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        for (OrderItem item : order.getItems()) {
            // Get size ID and quantity
            Long sizeId = item.getSize().getId();
            int quantity = item.getQuantity();
            String referenceNumber = "CANCEL-" + order.getOrderNumber() + "-" + "PRV-"+ item.getId().getProductVariantId() + "-" + "S-"+sizeId;

            if (oldStatus == OrderStatus.PENDING) {
                // For PENDING orders, release reserved stock
                inventoryService.releaseReservedStock(sizeId, quantity, referenceNumber, currentUser);
                log.info("Released reserved stock for cancelled PENDING order: {} units for size {}",
                        quantity, sizeId);
            } else if (oldStatus == OrderStatus.CONFIRMED) {
                // For CONFIRMED orders, return stock to inventory
                Optional<Inventory> inventoryOpt = inventoryRepository.findBySizeIdWithLock(sizeId);
                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();
                    int oldQuantity = inventory.getQuantityInStock();
                    inventory.setQuantityInStock(oldQuantity + quantity);
                    inventoryRepository.save(inventory);

                    // Create transaction log
                    StockTransaction transaction = StockTransaction.builder()
                            .inventory(inventory)
                            .transactionType(StockTransactionType.RETURN)
                            .quantity(quantity)
                            .orderId(order.getId())
                            .referenceNumber(referenceNumber)
                            .notes("Stock returned from cancelled order")
                            .createdBy(currentUser)
                            .build();
                    stockTransactionRepository.save(transaction);

                    log.info("Returned stock for cancelled CONFIRMED order: {} units for size {} (old: {}, new: {})",
                            quantity, sizeId, oldQuantity, inventory.getQuantityInStock());
                }
            }
        }
    }

    // Check if any shipping info fields have changed
    private boolean hasShippingInfoChanged(Order order, AdminUpdateOrderRequest request) {
        ShippingInfo info = order.getShippingInfo();
        return (request.fullName() != null && !request.fullName().equals(info.getFullName())) ||
                (request.phone() != null && !request.phone().equals(info.getPhone())) ||
                (request.country() != null && !request.country().equals(info.getCountry())) ||
                (request.city() != null && !request.city().equals(info.getCity())) ||
                (request.district() != null && !request.district().equals(info.getDistrict())) ||
                (request.ward() != null && !request.ward().equals(info.getWard())) ||
                (request.street() != null && !request.street().equals(info.getStreet()));
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

        // Store old status for email notification
        OrderStatus oldStatus = order.getStatus();
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

            // For PayPal orders that are being confirmed manually by admin,
            // send confirmation email if not already sent
            if (order.getPayment().getPaymentMethod() == PaymentMethod.PAYPAL &&
                    oldStatus == OrderStatus.PENDING) {
                try {
                    OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                            order,
                            order.getCustomer().getEmail(),
                            LanguageUtils.getCurrentLanguage()
                    );
                    log.info("Sending order confirmation email for admin-confirmed PayPal order");
                    emailService.sendOrderConfirmationEmail(orderResponse, order.getCustomer().getEmail());
                    return; // Don't send status update email since we sent confirmation email
                } catch (Exception e) {
                    log.error("Failed to send order confirmation email for admin confirmation: {}", e.getMessage());
                }
            }
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

        // Send status update email notification (async) - but only if not PENDING to CONFIRMED for PayPal
        if (!(oldStatus == OrderStatus.PENDING && target == OrderStatus.CONFIRMED &&
                order.getPayment().getPaymentMethod() == PaymentMethod.PAYPAL)) {
            try {
                OrderResponse orderResponse = orderMapper.mapToOrderResponse(
                        order,
                        order.getCustomer().getEmail(),
                        LanguageUtils.getCurrentLanguage()
                );
                emailService.sendOrderStatusUpdateEmail(
                        orderResponse,
                        order.getCustomer().getEmail(),
                        oldStatus,
                        target
                );
            } catch (Exception e) {
                log.error("Failed to send order status update email: {}", e.getMessage());
            }
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
                if (getAllowedStatusFlow().get(order.getStatus()).contains(OrderStatus.CANCELED)) {
                    order.setStatus(OrderStatus.CANCELED);
                }
            } else {
                throw new OrderException("Unsupported payment status transition from PENDING to " + target);
            }
        } else {
            throw new OrderException("Payment status is terminal and cannot be changed");
        }
    }

    private static Map<OrderStatus, Set<OrderStatus>> getAllowedStatusFlow() {
        return ALLOWED_STATUS_FLOW;
    }

    /**
     * Clear cart items associated with an order after successful payment
     */
    private void clearCartItemsFromOrder(Order order) {
        try {
            // Extract cart item IDs from order notes
            String notes = order.getNotes();
            if (notes != null && notes.contains("CART_ITEMS:")) {
                String cartItemIdsStr = notes.substring(notes.indexOf("CART_ITEMS:") + 11);
                // Remove any additional notes after cart items
                if (cartItemIdsStr.contains(" | ")) {
                    cartItemIdsStr = cartItemIdsStr.substring(0, cartItemIdsStr.indexOf(" | "));
                }

                String[] cartItemIds = cartItemIdsStr.split(",");
                List<Long> ids = Arrays.stream(cartItemIds)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList();

                if (!ids.isEmpty()) {
                    List<CartItem> cartItems = cartItemRepository.findAllById(ids);
                    cartItemRepository.deleteAll(cartItems);
                    log.info("Cleared {} cart items after successful PayPal payment", cartItems.size());
                }
            }
        } catch (Exception e) {
            log.error("Error clearing cart items from order {}: {}", order.getOrderNumber(), e.getMessage());
            // Don't throw exception as payment is already successful
        }
    }
}
