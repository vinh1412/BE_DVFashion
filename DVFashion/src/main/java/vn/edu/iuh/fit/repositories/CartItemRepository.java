/*
 * @ {#} CartItemRepository.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.CartItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository for managing cart items
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    /**
     * Finds cart items with reservations that have expired.
     *
     * @param now the current time to compare against reservedUntil
     * @return list of cart items with expired reservations
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.reservedUntil < :now")
    List<CartItem> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Finds a cart item by user ID and size ID.
     *
     * @param userId the ID of the user
     * @param sizeId the ID of the size
     * @return an Optional containing the found cart item, or empty if not found
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.size.id = :sizeId")
    Optional<CartItem> findByUserIdAndSizeId(@Param("userId") Long userId, @Param("sizeId") Long sizeId);

    /**
     * Finds all cart items for a given user ID.
     *
     * @param userId the ID of the user
     * @return list of cart items belonging to the user
     */
    List<CartItem> findByCartUserId(Long userId);

    /**
     * Counts the number of cart items in a cart by cart ID.
     *
     * @param cartId the ID of the cart
     * @return the count of cart items in the specified cart
     */
    int countByCartId(Long cartId);
}
