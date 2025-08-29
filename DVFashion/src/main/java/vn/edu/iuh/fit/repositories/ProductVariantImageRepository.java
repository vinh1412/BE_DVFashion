/*
 * @ {#} ProductVariantRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ProductVariantImage;

import java.util.List;

/*
 * @description: Repository interface for managing ProductVariantImage entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Long> {
    /**
     * Finds all ProductVariantImage entities associated with a specific ProductVariant ID.
     *
     * @param variantId The ID of the ProductVariant.
     * @return A list of ProductVariantImage entities.
     */
    List<ProductVariantImage> findByProductVariantId(Long variantId);
}
