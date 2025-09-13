/*
 * @ {#} CartServiceImpl.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.AddToCartRequest;
import vn.edu.iuh.fit.dtos.request.UpdateCartItemQuantityRequest;
import vn.edu.iuh.fit.dtos.response.CartItemResponse;
import vn.edu.iuh.fit.dtos.response.CartResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.InsufficientStockException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.CartItemMapper;
import vn.edu.iuh.fit.mappers.ShoppingCartMapper;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.services.CartService;
import vn.edu.iuh.fit.services.InventoryService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * @description: Implementation of CartService for shopping cart operations
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;

    private final ShoppingCartRepository shoppingCartRepository;

    private final InventoryService inventoryService;

    private final SizeRepository sizeRepository;

    private final ProductVariantRepository productVariantRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        // Validate user exists
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validate size
        Size size = sizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Size not found "));

        // Validate product variant
        ProductVariant productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new NotFoundException("Product variant not found"));

        // Validate size belongs to product variant
        if (!size.getProductVariant().getId().equals(productVariant.getId())) {
            throw new NotFoundException("Size does not belong to the specified product variant");
        }

        // Get or create shopping cart for user
        ShoppingCart cart = getOrCreateCart(user);


        // Find existing cart item by user and size
        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndSizeId(user.getId(), request.sizeId());

        // Check if item already exists in cart
        if (existingItem.isPresent()) {
            // If exists, update quantity
            return updateCartItem(existingItem.get(), request.quantity());
        }

        // Generate reference number for stock reservation
        String referenceNumber = "CART_" + user.getId() + "_" + System.currentTimeMillis();

        // Reserve stock
        boolean reserved = inventoryService.reserveStock(request.sizeId(), request.quantity(), referenceNumber);

        // If reservation fails due to insufficient stock
        if (!reserved) {
            int availableQty = inventoryService.getAvailableQuantity(request.sizeId());
            throw new InsufficientStockException("Insufficient stock. Available: " + availableQty
                    + " , Requested: "+ request.quantity());
        }

        try {
            // Create cart item
            CartItem cartItem = CartItem.builder()
                    .quantity(request.quantity())
                    .unitPrice(calculateUnitPrice(productVariant))
                    .cart(cart)
                    .productVariant(productVariant)
                    .size(size)
                    .build();

            cartItemRepository.save(cartItem);

            log.info("Added {} items of size {} to cart for user {}",
                    request.quantity(), size.getSizeName(), user.getId());

            Language currentLanguage = LanguageUtils.getCurrentLanguage();
            return shoppingCartMapper.buildCartResponse(cart, currentLanguage);

        } catch (Exception e) {
            // If error occurs, release reserved stock
            inventoryService.releaseReservedStock(
                    request.sizeId(), request.quantity(), referenceNumber + "_ROLLBACK"
            );
            throw e;
        }
    }

    @Override
    public CartResponse getCart() {
        // Get user info
        UserResponse currentUserResponse = userService.getCurrentUser();

        // Validate user exists
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Find cart for user
        ShoppingCart cart = shoppingCartRepository.findByUserId(user.getId())
                .orElse(null);

        // If no cart, return empty cart response
        if (cart == null) {
            return new CartResponse(
                    null,
                    0,
                    BigDecimal.ZERO,
                    List.of()
            );
        }

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return shoppingCartMapper.buildCartResponse(cart, currentLanguage);
    }

    @Override
    public CartResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemQuantityRequest request) {
        // Validate newQuantity
        if (request.newQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        // Validate user exists
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Verify cart item belongs to current user
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Cart item does not belong to current user");
        }

        int currentQuantity = cartItem.getQuantity();
        int newQuantity = request.newQuantity();

        // If newQuantity is 0, remove item from cart
        if (newQuantity == 0) {
            return removeCartItem(cartItemId);
        }

        // Calculate quantity difference
        int quantityDiff = newQuantity - currentQuantity;

        String referenceNumber = "CART_UPDATE_QTY_" + cartItemId + "_" + System.currentTimeMillis();

        if (quantityDiff > 0) {
            // Need to reserve more stock
            boolean reserved = inventoryService.reserveStock(
                    cartItem.getSize().getId(), quantityDiff, referenceNumber);

            if (!reserved) {
                int availableQty = inventoryService.getAvailableQuantity(cartItem.getSize().getId());
                throw new InsufficientStockException(
                        String.format("Insufficient stock. Available: %d, Requested: %d",
                                availableQty, newQuantity)
                );
            }
        } else if (quantityDiff < 0) {
            // Need to release some reserved stock
            inventoryService.releaseReservedStock(
                    cartItem.getSize().getId(), Math.abs(quantityDiff), referenceNumber);
        }

        try {
            // Update quantity
            cartItem.setQuantity(newQuantity);
            cartItem.setReservedUntil(LocalDateTime.now().plusMinutes(30));
            cartItemRepository.save(cartItem);

            log.info("Updated cart item {} quantity from {} to {} for user {}",
                    cartItemId, currentQuantity, newQuantity, user.getId());

            Language currentLanguage = LanguageUtils.getCurrentLanguage();
            return shoppingCartMapper.buildCartResponse(cartItem.getCart(), currentLanguage);

        } catch (Exception e) {
            // Rollback stock changes if error occurs
            if (quantityDiff > 0) {
                inventoryService.releaseReservedStock(
                        cartItem.getSize().getId(), quantityDiff, referenceNumber + "_ROLLBACK");
            } else if (quantityDiff < 0) {
                inventoryService.reserveStock(
                        cartItem.getSize().getId(), Math.abs(quantityDiff), referenceNumber + "_ROLLBACK");
            }
            throw e;
        }
    }

    @Override
    public CartResponse removeCartItem(Long cartItemId) {
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Cart item does not belong to current user");
        }

        // Release reserved stock
        String referenceNumber = "CART_REMOVE_" + cartItemId + "_" + System.currentTimeMillis();
        inventoryService.releaseReservedStock(
                cartItem.getSize().getId(), cartItem.getQuantity(), referenceNumber);

        cartItemRepository.delete(cartItem);

        log.info("Removed cart item {} for user {}", cartItemId, user.getId());

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return shoppingCartMapper.buildCartResponse(cartItem.getCart(), currentLanguage);
    }

    @Override
    public CartResponse clearCart() {
        // Validate user exists
        UserResponse currentUserResponse = userService.getCurrentUser();
        User user = userRepository.findById(currentUserResponse.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Find cart for user
        ShoppingCart cart = shoppingCartRepository.findByUserId(user.getId())
                .orElse(null);

        // If no cart exists, return empty cart response
        if (cart == null) {
            return new CartResponse(
                    null,
                    0,
                    BigDecimal.ZERO,
                    List.of()
            );
        }

        // Get all cart items
        List<CartItem> cartItems = cartItemRepository.findByCartUserId(user.getId());

        // If cart is already empty
        if (cartItems.isEmpty()) {
            Language currentLanguage = LanguageUtils.getCurrentLanguage();
            return shoppingCartMapper.buildCartResponse(cart, currentLanguage);
        }

        // Release reserved stock for all items
        String referenceNumber = "CART_CLEAR_" + user.getId() + "_" + System.currentTimeMillis();

        for (CartItem item : cartItems) {
            inventoryService.releaseReservedStock(
                    item.getSize().getId(),
                    item.getQuantity(),
                    referenceNumber + "_ITEM_" + item.getId()
            );
        }

        // Delete all cart items
        cartItemRepository.deleteAll(cartItems);

        log.info("Cleared cart for user {} - removed {} items", user.getId(), cartItems.size());

        Language currentLanguage = LanguageUtils.getCurrentLanguage();
        return shoppingCartMapper.buildCartResponse(cart, currentLanguage);
    }

    // Update existing cart item quantity
    private CartResponse updateCartItem(CartItem existingItem, int additionalQuantity) {
        // Get current quantity
        int currentQuantity = existingItem.getQuantity();

        // Calculate new total quantity
        int newTotalQuantity = currentQuantity + additionalQuantity; // Cộng dồn thay vì replace

        // Quantity diff need to reserve more
        int quantityDiff = additionalQuantity;

        // Generate reference number for this transaction
        String referenceNumber = "CART_UPDATE_" + existingItem.getId() + "_" + System.currentTimeMillis();

        boolean reserved = inventoryService.reserveStock(existingItem.getSize().getId(), quantityDiff, referenceNumber);

        // If reservation fails due to insufficient stock
        if (!reserved) {
            int availableQty = inventoryService.getAvailableQuantity(existingItem.getSize().getId());
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested additional: %d",
                            availableQty, quantityDiff)
            );
        }

        try {
            // Update quantity for existing item
            existingItem.setQuantity(newTotalQuantity);

            // Extend reservation time
            existingItem.setReservedUntil(LocalDateTime.now().plusMinutes(30));

            cartItemRepository.save(existingItem);

            log.info("Updated cart item {} from {} to {} quantity for user {}",
                    existingItem.getId(), currentQuantity, newTotalQuantity,
                    existingItem.getCart().getUser().getId());

            Language currentLanguage = LanguageUtils.getCurrentLanguage();
            return shoppingCartMapper.buildCartResponse(existingItem.getCart(), currentLanguage);

        } catch (Exception e) {
            // Rollback reserved stock if error occurs
            inventoryService.releaseReservedStock(
                    existingItem.getSize().getId(), quantityDiff, referenceNumber + "_ROLLBACK"
            );
            throw e;
        }
    }

    // Get existing cart or create new one
    private ShoppingCart getOrCreateCart(User user) {
        return shoppingCartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ShoppingCart newCart = ShoppingCart.builder()
                            .user(user)
                            .build();
                    return shoppingCartRepository.save(newCart);
                });
    }

    // Calculate unit price based on product and variant
    private BigDecimal calculateUnitPrice(ProductVariant productVariant) {
        // Get product from variant
        Product product = productVariant.getProduct();

        // Base price is sale price if on sale, otherwise regular price
        BigDecimal basePrice = product.isOnSale() && product.getSalePrice() != null
                ? product.getSalePrice()
                : product.getPrice();

        // Add additional price from variant if any
        BigDecimal additionalPrice = productVariant.getAddtionalPrice() != null
                ? productVariant.getAddtionalPrice()
                : BigDecimal.ZERO;

        return basePrice.add(additionalPrice);
    }
}
