/*
 * @ {#} ShoppingCart.java   1.0     10/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.CartItemResponse;
import vn.edu.iuh.fit.dtos.response.CartResponse;
import vn.edu.iuh.fit.entities.CartItem;
import vn.edu.iuh.fit.entities.ShoppingCart;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.repositories.CartItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @description: Mapper class for converting between ShoppingCart entity and DTOs
 * @author: Tran Hien Vinh
 * @date:   10/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class ShoppingCartMapper {
    private final CartItemRepository cartItemRepository;

    private final CartItemMapper cartItemMapper;

    public CartResponse buildCartResponse(ShoppingCart cart, Language language) {
        List<CartItem> items = cartItemRepository.findByCartUserId(cart.getUser().getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> cartItemMapper.mapToCartItemResponse(item, language))
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartResponse(
                cart.getId(),
                totalItems,
                totalAmount,
                itemResponses
        );
    }
}
