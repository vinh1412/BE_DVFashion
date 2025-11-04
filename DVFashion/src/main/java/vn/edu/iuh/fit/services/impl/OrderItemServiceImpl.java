/*
 * @ {#} OrderItemServiceImpl.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.services.OrderItemService;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Implementation of OrderItemService for managing order items
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    @Override
    public List<OrderItem> createOrderItems(List<CartItem> cartItems, Order order) {
        return cartItems.stream().map(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .id(new OrderItemId(cartItem.getProductVariant().getId(), order.getId(), cartItem.getSize().getId()))
                    .productVariant(cartItem.getProductVariant())
                    .order(order)
                    .size(cartItem.getSize())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
//                    .discount(calculateDiscount(cartItem, order.getPromotion()))
                    .discount(BigDecimal.ZERO)
                    .build();

            return orderItem;
        }).toList();
    }

    private BigDecimal calculateUnitPrice(CartItem cartItem) {
        BigDecimal basePrice = cartItem.getUnitPrice();
        BigDecimal additionalPrice = cartItem.getProductVariant().getAddtionalPrice() != null
                ? cartItem.getProductVariant().getAddtionalPrice()
                : BigDecimal.ZERO;
        return basePrice.add(additionalPrice);
    }

    private BigDecimal calculateDiscount(CartItem cartItem, Promotion promotion) {
        if (promotion == null) return BigDecimal.ZERO;

//        BigDecimal unitPrice = calculateUnitPrice(cartItem);
        BigDecimal unitPrice = cartItem.getUnitPrice();
        return switch (promotion.getType()) {
//            case PERCENTAGE -> unitPrice.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100));
//            case FIXED_AMOUNT -> promotion.getValue();
            default -> BigDecimal.ZERO;
        };
    }

    @Override
    public BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getUnitPrice().subtract(item.getDiscount())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
