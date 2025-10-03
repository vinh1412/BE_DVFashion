/*
 * @ {#} OrderMapper.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;

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
                order.getShippedDate(),
                order.getDeliveredDate(),
                orderItemMapper.mapOrderItemResponses(order.getItems(), language),
                paymentMapper.mapPaymentResponse(order.getPayment()),
                order.getPromotion() != null ? promotionMapper.mapPromotionOrderResponse(order.getPromotion(), language) : null
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
}
