/*
 * @ {#} ProductVariantRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ProductVariant;

/*
 * @description: Repository interface for managing Product Variant entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    /**
     * Checks if a product variant with the specified product ID and color (case-insensitive) exists.
     *
     * @param productId The ID of the product.
     * @param color     The color of the product variant.
     * @return true if a matching product variant exists, false otherwise.
     */
    boolean existsByProductIdAndColorIgnoreCase(Long productId, String color);

    /**
     * Checks if any product variant exists for the specified product ID.
     *
     * @param productId The ID of the product.
     * @return true if any product variant exists for the product ID, false otherwise.
     */
    boolean existsByProductId(Long productId);
}
