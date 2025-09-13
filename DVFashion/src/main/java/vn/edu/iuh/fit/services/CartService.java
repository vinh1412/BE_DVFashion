/*
 * @ {#} CartService.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.AddToCartRequest;
import vn.edu.iuh.fit.dtos.request.UpdateCartItemQuantityRequest;
import vn.edu.iuh.fit.dtos.response.CartResponse;

/*
 * @description: Service class for shopping cart operations
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
public interface CartService {
    /**
     * Add a product variant to the user's shopping cart.
     *
     * @param request the request containing product variant ID, size ID, and quantity
     * @return the updated cart response
     */
    CartResponse addToCart(AddToCartRequest request);

    /**
     * Retrieve the current state of the user's shopping cart.
     *
     * @return the cart response
     */
    CartResponse getCart();

    /**
     * Update the quantity of a specific cart item.
     *
     * @param cartItemId the ID of the cart item to update
     * @param request    the request containing the new quantity
     * @return the updated cart response
     */
    CartResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemQuantityRequest request);

    /**
     * Remove a specific item from the shopping cart.
     *
     * @param cartItemId the ID of the cart item to remove
     * @return the updated cart response
     */
    CartResponse removeCartItem(Long cartItemId);

    /**
     * Clear all items from the shopping cart.
     *
     * @return the updated (empty) cart response
     */
    CartResponse clearCart();
}
