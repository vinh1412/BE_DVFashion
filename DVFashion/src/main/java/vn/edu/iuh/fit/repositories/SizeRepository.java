/*
 * @ {#} ProductVariantRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ProductVariant;
import vn.edu.iuh.fit.entities.Size;

import java.util.List;

/*
 * @description: Repository interface for managing Size entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    /**
     * Checks if a Size with the given ProductVariant and sizeName already exists.
     *
     * @param productVariant The ProductVariant entity.
     * @param sizeName       The name of the size.
     * @return true if a Size with the specified ProductVariant and sizeName exists, false otherwise.
     */
    boolean existsByProductVariantAndSizeName(ProductVariant productVariant, String sizeName);

    /**
     * Finds all Size entities associated with a specific ProductVariant ID.
     *
     * @param variantId The ID of the ProductVariant.
     * @return A list of Size entities.
     */
    List<Size> findByProductVariantId(Long variantId);
}
