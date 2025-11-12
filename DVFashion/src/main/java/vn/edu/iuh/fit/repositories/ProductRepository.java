/*
 * @ {#} ProductRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.PromotionProduct;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing Product entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
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

    /**
     * Finds existing product IDs from a list of IDs.
     *
     * @param ids the list of product IDs to check
     * @return list of existing product IDs
     */
    @Query("SELECT p.id FROM Product p WHERE p.id IN :ids")
    List<Long> findExistingProductIds(@Param("ids") List<Long> ids);

    /**
     * Finds the best active promotion for a given product ID.
     *
     * @param productId   the ID of the product
     * @param currentTime the current time to check promotion validity
     * @return an Optional containing the best active PromotionProduct, or empty if none found
     */
    @Query("""
    SELECT pp FROM PromotionProduct pp
    JOIN pp.promotion p
    WHERE pp.product.id = :productId
    AND pp.active = true
    AND p.active = true
    AND p.startDate <= :currentTime
    AND p.endDate >= :currentTime
    AND pp.stockQuantity > pp.soldQuantity
    ORDER BY pp.discountPercentage DESC
    LIMIT 1
    """)
    Optional<PromotionProduct> findBestActivePromotionByProductId(
            @Param("productId") Long productId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Finds products associated with a specific promotion ID with pagination.
     *
     * @param promotionId the ID of the promotion
     * @param pageable    pagination information
     * @return a page of products associated with the given promotion ID
     */
    @Query("SELECT pp.product FROM PromotionProduct pp WHERE pp.promotion.id = :promotionId")
    Page<Product> findProductsByPromotionId(@Param("promotionId") Long promotionId, Pageable pageable);

    /**
     * Finds products by category ID.
     *
     * @param categoryId the ID of the category
     * @return a list of products in the specified category
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Finds products by category ID with pagination.
     *
     * @param categoryId the ID of the category
     * @param pageable   pagination information
     * @return a page of products in the specified category
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Counts the total number of products.
     *
     * @return the total number of products
     */
    @Query("SELECT COUNT(p) FROM Product p")
    long countTotalProducts();

    /**
     * Counts the number of products by their status.
     *
     * @param status the status to filter by
     * @return the number of products with the specified status
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countProductsByStatus(@Param("status") ProductStatus status);

    /**
     * Counts the total number of product variants.
     *
     * @return the total number of product variants
     */
    @Query("SELECT COUNT(pv) FROM ProductVariant pv")
    long countTotalProductVariants();

    /**
     * Counts the number of products that have low stock levels.
     *
     * @return the number of products with low stock
     */
    @Query("""
        SELECT COUNT(DISTINCT pv.product.id)
        FROM Inventory i
        JOIN i.size.productVariant pv
        WHERE i.quantityInStock <= i.minStockLevel
    """)
    long countProductsWithLowStock();

    /**
     * Counts the number of products that are out of stock.
     *
     * @return the number of out-of-stock products
     */
    @Query("""
        SELECT COUNT(DISTINCT pv.product.id)
        FROM Inventory i
        JOIN i.size.productVariant pv
        WHERE i.quantityInStock = 0
    """)
    long countOutOfStockProducts();

    /**
     * Counts the number of products that are currently on promotion.
     *
     * @return the number of products on promotion
     */
    @Query("SELECT p.status, COUNT(p) FROM Product p GROUP BY p.status")
    List<Object[]> countProductsByAllStatuses();

    /**
     * Counts the number of products that are currently on active promotion.
     *
     * @return the number of products on active promotion
     */
    @Query("""
        SELECT COUNT(DISTINCT pp.product.id)
        FROM PromotionProduct pp
        JOIN pp.promotion p
        WHERE pp.active = true
        AND p.active = true
        AND p.startDate <= CURRENT_TIMESTAMP
        AND p.endDate >= CURRENT_TIMESTAMP
        AND pp.stockQuantity > pp.soldQuantity
    """)
    long countProductsOnActivePromotion();

}
