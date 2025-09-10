/*
 * @ {#} ShoppingCartRepository.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ShoppingCart;

import java.util.Optional;

/*
 * @description: Repository interface for shopping cart operations
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    /**
     * Finds a shopping cart by user ID.
     *
     * @param userId the ID of the user
     * @return an Optional containing the found shopping cart, or empty if not found
     */
    Optional<ShoppingCart> findByUserId(Long userId);
}
