/*
 * @ {#} OrderMapper.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.request.AdminUpdateOrderRequest;
import vn.edu.iuh.fit.dtos.request.UpdateOrderByUserRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;
import vn.edu.iuh.fit.exceptions.OrderException;

import java.math.BigDecimal;

/*
 * @description: Mapper class for converting Order entities to OrderResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ShippingInfoMapper shippingInfoMapper;
    private final PaymentMapper paymentMapper;
    private final PromotionMapper promotionMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderResponse mapToOrderResponse(Order order, String userEmail, Language language) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getFullName(),
                order.getStatus(),
                calculateSubtotal(order),
                order.getShippingFee(),
                calculateDiscountAmount(order),
                order.getPayment().getAmount(),
                shippingInfoMapper.mapShippingInfoResponse(order.getShippingInfo(), userEmail),
                order.getNotes(),
                order.getOrderDate(),
                order.getEstimatedDeliveryTime(),
                orderItemMapper.mapOrderItemResponses(order.getItems(), language),
                paymentMapper.mapPaymentResponse(order.getPayment()),
//                order.getPromotion() != null ? promotionMapper.mapPromotionOrderResponse(order.getPromotion(), language) : null,
                null,
                order.getPayment().getPaymentMethod() == PaymentMethod.PAYPAL
                        && order.getPayment().getPaymentStatus() == PaymentStatus.PENDING ? order.getPayment().getApprovalUrl() : null
        );
    }

    private BigDecimal calculateSubtotal(Order order) {
        return order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscountAmount(Order order) {
        return order.getItems().stream()
                .map(item -> item.getDiscount().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Ensure shipping info can be updated only when order is PENDING or PROCESSING
    public void ensureShippingUpdatable(OrderStatus status) {
        if (!(status == OrderStatus.PENDING || status == OrderStatus.PROCESSING)) {
            throw new OrderException("Shipping address can be updated only when order is PENDING or PROCESSING");
        }
    }

    // Partial update of shipping info fields (if provided)
    public void applyShippingInfo(Order order, UpdateOrderByUserRequest req, AdminUpdateOrderRequest adminUpdateOrderRequest) {
        if (req != null) {
            if (req.fullName() != null && !req.fullName().isBlank()) order.getShippingInfo().setFullName(req.fullName());
            if (req.phone() != null && !req.phone().isBlank()) order.getShippingInfo().setPhone(req.phone());
            if (req.country() != null && !req.country().isBlank()) order.getShippingInfo().setCountry(req.country());
            if (req.city() != null && !req.city().isBlank()) order.getShippingInfo().setCity(req.city());
            if (req.ward() != null && !req.ward().isBlank()) order.getShippingInfo().setWard(req.ward());
            if (req.district() != null && !req.district().isBlank()) order.getShippingInfo().setDistrict(req.district());
            if (req.street() != null && !req.street().isBlank()) order.getShippingInfo().setStreet(req.street());
        }

        if (adminUpdateOrderRequest != null) {
            if (adminUpdateOrderRequest.fullName() != null && !adminUpdateOrderRequest.fullName().isBlank()) order.getShippingInfo().setFullName(adminUpdateOrderRequest.fullName());
            if (adminUpdateOrderRequest.phone() != null && !adminUpdateOrderRequest.phone().isBlank()) order.getShippingInfo().setPhone(adminUpdateOrderRequest.phone());
            if (adminUpdateOrderRequest.country() != null && !adminUpdateOrderRequest.country().isBlank()) order.getShippingInfo().setCountry(adminUpdateOrderRequest.country());
            if (adminUpdateOrderRequest.city() != null && !adminUpdateOrderRequest.city().isBlank()) order.getShippingInfo().setCity(adminUpdateOrderRequest.city());
            if (adminUpdateOrderRequest.ward() != null && !adminUpdateOrderRequest.ward().isBlank()) order.getShippingInfo().setWard(adminUpdateOrderRequest.ward());
            if (adminUpdateOrderRequest.district() != null && !adminUpdateOrderRequest.district().isBlank()) order.getShippingInfo().setDistrict(adminUpdateOrderRequest.district());
            if (adminUpdateOrderRequest.street() != null && !adminUpdateOrderRequest.street().isBlank()) order.getShippingInfo().setStreet(adminUpdateOrderRequest.street());
        }
    }
}
