/*
 * @ {#} PromotionProductRepository.java   1.0     01/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.PromotionProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing PromotionProduct entities
 * @author: Tran Hien Vinh
 * @date:   01/11/2025
 * @version:    1.0
 */
@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {
    /**
     * Finds all PromotionProduct entities by the given promotion ID.
     *
     * @param promotionId the ID of the promotion
     * @return a list of PromotionProduct entities associated with the promotion ID
     */
    List<PromotionProduct> findByPromotionId(Long promotionId);

    /**
     * Finds all PromotionProduct entities by the given product ID.
     *
     * @param productId the ID of the product
     * @return a list of PromotionProduct entities associated with the product ID
     */
    List<PromotionProduct> findByProductId(Long productId);

    /**
     * Finds a PromotionProduct entity by the given promotion ID and product ID.
     *
     * @param promotionId the ID of the promotion
     * @param productId   the ID of the product
     * @return an Optional containing the PromotionProduct entity if found, or empty if not found
     */
    Optional<PromotionProduct> findByPromotionIdAndProductId(Long promotionId, Long productId);

    /**
     * Finds all active PromotionProduct entities by the given promotion ID.
     *
     * @param promotionId the ID of the promotion
     * @return a list of active PromotionProduct entities associated with the promotion ID
     */
    @Query("SELECT pp FROM PromotionProduct pp WHERE pp.promotion.id = :promotionId AND pp.active = true")
    List<PromotionProduct> findActiveByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * Finds an active PromotionProduct for a given product ID, where the associated promotion is also active
     * and within the valid date range, and there is available stock.
     *
     * @param productId   the ID of the product
     * @param currentTime the current date and time
     * @return an Optional containing the active PromotionProduct, or empty if none found
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
        """)
    Optional<PromotionProduct> findActivePromotionByProductId(
            @Param("productId") Long productId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Finds active PromotionProduct entities for a list of product IDs, where the associated promotions are also active
     * and within the valid date range, and there is available stock.
     *
     * @param productIds  the list of product IDs
     * @param currentTime the current date and time
     * @return a list of active PromotionProduct entities
     */
    @Query("""
        SELECT pp FROM PromotionProduct pp 
        JOIN pp.promotion p 
        WHERE pp.product.id IN :productIds 
        AND pp.active = true 
        AND p.active = true 
        AND p.startDate <= :currentTime 
        AND p.endDate >= :currentTime 
        AND pp.stockQuantity > pp.soldQuantity
        """)
    List<PromotionProduct> findActivePromotionsByProductIds(
            @Param("productIds") List<Long> productIds,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Finds the lowest active promotion price for a given product ID.
     *
     * @param productId the ID of the product
     * @return an Optional containing the lowest active promotion price, or empty if none found
     */
    @Query("SELECT pp.promotionPrice FROM PromotionProduct pp " +
            "WHERE pp.product.id = :productId " +
            "AND pp.active = true " +
            "AND pp.promotion.active = true " +
            "AND pp.promotion.startDate <= CURRENT_TIMESTAMP " +
            "AND pp.promotion.endDate >= CURRENT_TIMESTAMP " +
            "ORDER BY pp.promotionPrice ASC " +
            "LIMIT 1")
    Optional<BigDecimal> findActivePromotionPrice(@Param("productId") Long productId);

    /**
     * Finds an active promotion product for a given product ID.
     *
     * @param productId the ID of the product
     * @return an Optional containing the active PromotionProduct, or empty if none found
     */
    @Query("SELECT pp FROM PromotionProduct pp " +
            "WHERE pp.product.id = :productId " +
            "AND pp.active = true " +
            "AND pp.promotion.active = true " +
            "AND pp.promotion.startDate <= CURRENT_TIMESTAMP " +
            "AND pp.promotion.endDate >= CURRENT_TIMESTAMP")
    Optional<PromotionProduct> findActivePromotionForProduct(@Param("productId") Long productId);
}
