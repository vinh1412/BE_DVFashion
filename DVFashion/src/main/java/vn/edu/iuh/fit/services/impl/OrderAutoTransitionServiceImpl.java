/*
 * @ {#} OrderAutoTransitionServiceImpl.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.config.OrderAutoTransitionConfig;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.entities.Order;
import vn.edu.iuh.fit.entities.OrderAutoTransition;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.OrderException;
import vn.edu.iuh.fit.mappers.OrderMapper;
import vn.edu.iuh.fit.repositories.OrderAutoTransitionRepository;
import vn.edu.iuh.fit.repositories.OrderRepository;
import vn.edu.iuh.fit.services.EmailService;
import vn.edu.iuh.fit.services.InventoryService;
import vn.edu.iuh.fit.services.OrderAutoTransitionService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/*
 * @description: Service implementation for automatic order status transitions
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderAutoTransitionServiceImpl implements OrderAutoTransitionService {
    private final OrderRepository orderRepository;

    private final OrderAutoTransitionRepository autoTransitionRepository;

    private final OrderAutoTransitionConfig config;

    private final InventoryService inventoryService;

    private final EmailService emailService;

    private final OrderMapper orderMapper;

    // Mapping transition types to status flows
    private static final Map<AutoTransitionType, StatusTransition> TRANSITION_MAPPINGS = Map.of(
            AutoTransitionType.CONFIRMED_TO_PROCESSING,
            new StatusTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING),
            AutoTransitionType.PROCESSING_TO_SHIPPED,
            new StatusTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED),
            AutoTransitionType.SHIPPED_TO_DELIVERED,
            new StatusTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED),
            AutoTransitionType.PENDING_TO_CANCELLED,
            new StatusTransition(OrderStatus.PENDING, OrderStatus.CANCELED)
    );

    @Override
    public void scheduleAutoTransition(Order order, AutoTransitionType transitionType) {
        if (!config.isEnabled()) {
            log.debug("Auto transition is disabled");
            return;
        }

        // Check if transition already scheduled
        if (autoTransitionRepository.existsByOrderIdAndTransitionTypeAndIsExecutedFalse(
                order.getId(), transitionType)) {
            log.debug("Auto transition already scheduled for order {} and type {}",
                    order.getOrderNumber(), transitionType);
            return;
        }

        // Calculate scheduled time
        Duration delay = getTransitionDelay(transitionType);
        LocalDateTime scheduledTime = calculateScheduledTime(LocalDateTime.now(), delay);

        // Get expected status transition
        StatusTransition statusTransition = TRANSITION_MAPPINGS.get(transitionType);
        if (statusTransition == null) {
            throw new OrderException("Unknown transition type: " + transitionType);
        }

        // Validate current order status
        if (order.getStatus() != statusTransition.fromStatus) {
            log.warn("Cannot schedule transition {} for order {} - current status {} does not match expected {}",
                    transitionType, order.getOrderNumber(), order.getStatus(), statusTransition.fromStatus);
            return;
        }

        OrderAutoTransition autoTransition = OrderAutoTransition.builder()
                .order(order)
                .transitionType(transitionType)
                .fromStatus(statusTransition.fromStatus)
                .toStatus(statusTransition.toStatus)
                .scheduledAt(scheduledTime)
                .build();

        autoTransitionRepository.save(autoTransition);

        log.info("Scheduled auto transition {} for order {} at {}",
                transitionType, order.getOrderNumber(), scheduledTime);
    }

    @Override
    public void executeScheduledTransitions() {
        // Check if auto transition is enabled
        if (!config.isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Skip execution outside business hours if configured
        if (config.isRespectBusinessHours() && !isBusinessHours(now)) {
            log.debug("Skipping auto transition execution outside business hours");
            return;
        }

        // Fetch pending transitions
        List<OrderAutoTransition> pendingTransitions =
                autoTransitionRepository.findPendingTransitions(now);

        log.info("Found {} pending auto transitions to execute", pendingTransitions.size());

        // Execute each transition
        for (OrderAutoTransition transition : pendingTransitions) {
            executeTransition(transition);
        }
    }

    // Execute a single auto transition
    private void executeTransition(OrderAutoTransition transition) {
        try {
            Order order = transition.getOrder();
            AutoTransitionType type = transition.getTransitionType();

            log.info("Executing auto transition {} for order {}",
                    transition.getTransitionType(), order.getOrderNumber());

            // Validate pre-conditions
            if (!validateTransitionConditions(order, transition.getTransitionType())) {
                markTransitionFailed(transition, "Pre-conditions not met");
                return;
            }

            // Execute the actual status change
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(transition.getToStatus());

            // Perform any additional business logic based on transition type
            performAdditionalTransitionLogic(order, transition.getTransitionType());

            // Save order
            orderRepository.save(order);

            // Mark transition as completed
            markTransitionCompleted(transition);

            scheduleNextTransition(order, type);

            // Send notification if configured
            if (config.isNotifyCustomerOnTransition()) {
                sendTransitionNotification(order, oldStatus, transition.getToStatus());
            }

            log.info("Successfully executed auto transition {} for order {}",
                    transition.getTransitionType(), order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to execute auto transition for order {}: {}",
                    transition.getOrder().getOrderNumber(), e.getMessage(), e);
            markTransitionFailed(transition, e.getMessage());
        }
    }

    // Schedule the next transition in the chain if applicable
    private void scheduleNextTransition(Order order, AutoTransitionType completedType) {
        switch (completedType) {
            case CONFIRMED_TO_PROCESSING ->
                    scheduleAutoTransition(order, AutoTransitionType.PROCESSING_TO_SHIPPED);

            case PROCESSING_TO_SHIPPED ->
                    scheduleAutoTransition(order, AutoTransitionType.SHIPPED_TO_DELIVERED);

            case SHIPPED_TO_DELIVERED ->
                    log.info("Order {} fully delivered – end of chain", order.getOrderNumber());

            default -> { }
        }
    }

    // Validate pre-conditions for the transition
    private boolean validateTransitionConditions(Order order, AutoTransitionType transitionType) {
        // Validate order still exists and is in expected status
        StatusTransition statusTransition = TRANSITION_MAPPINGS.get(transitionType);
        if (order.getStatus() != statusTransition.fromStatus) {
            log.warn("Order {} status changed to {} - expected {}",
                    order.getOrderNumber(), order.getStatus(), statusTransition.fromStatus);
            return false;
        }

        // Specific validations per transition type
        switch (transitionType) {
            case CONFIRMED_TO_PROCESSING:
                return validateConfirmedToProcessing(order);

            case PROCESSING_TO_SHIPPED:
                return validateProcessingToShipped(order);

            case SHIPPED_TO_DELIVERED:
                return validateShippedToDelivered(order);

            case PENDING_TO_CANCELLED:
                return validatePendingToCancelled(order);

            default:
                return true;
        }
    }

    // Specific validation methods for each transition type
    private boolean validateConfirmedToProcessing(Order order) {
        // Check payment status if required
        if (order.getPayment().getPaymentMethod() == PaymentMethod.PAYPAL) {
            if (order.getPayment().getPaymentStatus() != PaymentStatus.COMPLETED) {
                log.warn("Order {} requires PayPal payment completion", order.getOrderNumber());
                return false;
            }

            return true;
        }

        return true;
    }

    // Specific validation for Processing to Shipped
    private boolean validateProcessingToShipped(Order order) {
        // Ensure all items are available and allocated
        return true;
    }

    // Specific validation for Shipped to Delivered
    private boolean validateShippedToDelivered(Order order) {
        // Additional validation can be added here
        return true;
    }

    // Specific validation for Pending to Cancelled
    private boolean validatePendingToCancelled(Order order) {
        // Only cancel if payment is still pending
        return order.getPayment() == null ||
                order.getPayment().getPaymentStatus() != PaymentStatus.COMPLETED;
    }

    // Perform additional business logic based on transition type
    private void performAdditionalTransitionLogic(Order order, AutoTransitionType transitionType) {
        switch (transitionType) {
            case PENDING_TO_CANCELLED:
                OrderStatus old = order.getStatus();
                User systemUser = order.getCustomer();

                if (old == OrderStatus.PENDING) {
                    // release reserved stock for pending orders
                    releaseReservedStock(order);
                } else if (old == OrderStatus.CONFIRMED) {
                    // restore stock for confirmed orders
                    inventoryService.restoreStockForConfirmedCancellation(order, systemUser);
                }
                cancelPaymentIfNeeded(order);
                break;
        }
    }

    // Release reserved stock for cancelled orders
    private void releaseReservedStock(Order order) {
        try {
            for (var item : order.getItems()) {
                String reference = "ORDER-" + order.getOrderNumber();
                inventoryService.releaseReservedStock(item.getSize().getId(),
                        item.getQuantity(), reference, order.getCustomer());
            }
            log.info("Released reserved stock for cancelled order {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to release stock for order {}: {}",
                    order.getOrderNumber(), e.getMessage());
        }
    }

    // Cancel payment if still pending
    private void cancelPaymentIfNeeded(Order order) {
        if (order.getPayment() != null &&
                order.getPayment().getPaymentStatus() == PaymentStatus.PENDING) {
            order.getPayment().setPaymentStatus(PaymentStatus.CANCELED);
            log.info("Cancelled payment for order {}", order.getOrderNumber());
        }
    }

    // Get transition delay from configuration
    private Duration getTransitionDelay(AutoTransitionType transitionType) {
        String configKey = transitionType.name();
        return config.getDelays().getOrDefault(configKey, Duration.ofHours(1));
    }

    // Calculate scheduled time considering business hours
    private LocalDateTime calculateScheduledTime(LocalDateTime from, Duration delay) {
        LocalDateTime scheduledTime = from.plus(delay);

        // Adjust for business hours if configured
        if (config.isRespectBusinessHours()) {
            scheduledTime = adjustToBusinessHours(scheduledTime);
        }

        return scheduledTime;
    }

    // Adjust datetime to fit within business hours
    private LocalDateTime adjustToBusinessHours(LocalDateTime dateTime) {
        // Skip weekends
        while (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
                dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dateTime = dateTime.plusDays(1);
        }

        // Adjust to business hours
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(LocalTime.of(config.getBusinessStartHour(), 0))) {
            dateTime = dateTime.with(LocalTime.of(config.getBusinessStartHour(), 0));
        } else if (time.isAfter(LocalTime.of(config.getBusinessEndHour(), 0))) {
            dateTime = dateTime.plusDays(1).with(LocalTime.of(config.getBusinessStartHour(), 0));
            // Check again for weekend
            return adjustToBusinessHours(dateTime);
        }

        return dateTime;
    }

    // Check if datetime is within business hours
    private boolean isBusinessHours(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(LocalTime.of(config.getBusinessStartHour(), 0)) &&
                time.isBefore(LocalTime.of(config.getBusinessEndHour(), 0));
    }

    // Mark transition as completed
    private void markTransitionCompleted(OrderAutoTransition transition) {
        transition.setExecuted(true);
        transition.setExecutedAt(LocalDateTime.now());
        transition.setExecutionResult("SUCCESS");
        autoTransitionRepository.save(transition);
    }

    // Mark transition as failed with reason
    private void markTransitionFailed(OrderAutoTransition transition, String reason) {
        transition.setExecuted(true);
        transition.setExecutedAt(LocalDateTime.now());
        transition.setExecutionResult("FAILED: " + reason);
        autoTransitionRepository.save(transition);
    }

    // Send notification to customer about the transition
    private void sendTransitionNotification(Order order, OrderStatus fromStatus, OrderStatus toStatus) {
        try {
            Language language = LanguageUtils.getCurrentLanguage();
            OrderResponse orderResponse = orderMapper.mapToOrderResponse(order,
                    order.getCustomer().getEmail(), language);
            // Send email notification to customer
            emailService.sendOrderStatusUpdateEmail(orderResponse, order.getCustomer().getEmail(), fromStatus, toStatus);
            log.info("Sent transition notification for order {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send transition notification for order {}: {}",
                    order.getOrderNumber(), e.getMessage());
        }
    }

    @Override
    public void cancelScheduledTransitions(Long orderId, AutoTransitionType transitionType) {
        List<OrderAutoTransition> pendingTransitions =
                autoTransitionRepository.findPendingTransitionsByOrderAndType(orderId, transitionType);

        for (OrderAutoTransition transition : pendingTransitions) {
            markTransitionFailed(transition, "Cancelled by system");
        }

        log.info("Cancelled {} pending transitions of type {} for order ID {}",
                pendingTransitions.size(), transitionType, orderId);
    }

    // Helper class for status transitions
    private record StatusTransition(OrderStatus fromStatus, OrderStatus toStatus) {}
}
