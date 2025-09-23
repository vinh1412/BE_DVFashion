/*
 * @ {#} ProductRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.util.List;

/*
 * @description: Repository interface for managing Product entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * Find products by category ID, excluding a specific product ID, and with a specific status.
     *
     * @param categoryId The ID of the category to filter products.
     * @param excludeId  The ID of the product to exclude from the results.
     * @param status     The status of the products to filter.
     * @return A list of products matching the criteria.
     */
    List<Product> findByCategoryIdAndIdNotAndStatus(Long categoryId, Long excludeId, ProductStatus status);

    /**
     * Find products excluding a list of product IDs, with a specific status, ordered by creation date descending.
     *
     * @param excludeIds A list of product IDs to exclude from the results.
     * @param status     The status of the products to filter.
     * @return A list of products matching the criteria.
     */
    List<Product> findByIdNotInAndStatusOrderByCreatedAtDesc(List<Long> excludeIds, ProductStatus status);

}
