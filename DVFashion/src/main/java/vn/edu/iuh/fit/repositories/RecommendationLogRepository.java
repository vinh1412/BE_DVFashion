/*
 * @ {#} RecommendationLogRepository.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.RecommendationLog;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Repository interface for managing RecommendationLog entities.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {
    /**
     * Finds the top recommended products since a given date.
     *
     * @param fromDate The date from which to consider recommendations.
     * @param pageable The pagination information.
     * @return A list of object arrays, each containing the product ID and recommendation count.
     */
    @Query("""
        SELECT rl.recommendedProductId as productId,
               COUNT(rl.recommendedProductId) as recommendationCount
        FROM RecommendationLog rl
        WHERE rl.createdAt >= :fromDate
        GROUP BY rl.recommendedProductId
        ORDER BY COUNT(rl.recommendedProductId) DESC
        """)
    List<Object[]> findTopRecommendedProducts(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    /**
     * Finds the top recommended products of all time.
     *
     * @param pageable The pagination information.
     * @return A list of object arrays, each containing the product ID and recommendation count.
     */
    @Query("""
        SELECT rl.recommendedProductId as productId,
               COUNT(rl.recommendedProductId) as recommendationCount
        FROM RecommendationLog rl
        GROUP BY rl.recommendedProductId
        ORDER BY COUNT(rl.recommendedProductId) DESC
        """)
    List<Object[]> findTopRecommendedProductsAllTime(Pageable pageable);

    /**
     * Counts the total number of distinct recommendations made since a given date.
     *
     * @param fromDate The date from which to count recommendations.
     * @return The total count of distinct recommendations.
     */
    @Query("""
    SELECT COUNT(DISTINCT rl.id) 
    FROM RecommendationLog rl 
    WHERE rl.createdAt >= :fromDate
    """)
    Long countTotalRecommendations(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Counts the total number of distinct recommendations made of all time.
     *
     * @return The total count of distinct recommendations.
     */
    @Query("""
    SELECT COUNT(DISTINCT rl.id) 
    FROM RecommendationLog rl
    """)
    Long countTotalRecommendationsAllTime();

    /**
     * Retrieves product recommendation statistics including counts of recommendations,
     * views, add-to-carts, and purchases since a given date.
     *
     * @param fromDate The date from which to gather statistics.
     * @param endDate  The end date for gathering statistics.
     * @param pageable The pagination information.
     * @return A list of object arrays, each containing the product ID, recommendation count,
     *         view count, add-to-cart count, and purchase count.
     */
    @Query("""
    SELECT rl.recommendedProductId,
           COUNT(DISTINCT rl.id),
           SUM(CASE WHEN upi.interactionType = 'VIEW' THEN 1 ELSE 0 END),
           SUM(CASE WHEN upi.interactionType = 'ADD_TO_CART' THEN 1 ELSE 0 END),
           SUM(CASE WHEN upi.interactionType = 'PURCHASE' THEN 1 ELSE 0 END)
    FROM RecommendationLog rl
    LEFT JOIN UserProductInteraction upi 
           ON rl.recommendedProductId = upi.product.id 
          AND rl.userId = upi.user.id
          AND upi.createdAt >= rl.createdAt
          AND upi.createdAt <= :endDate
    WHERE rl.createdAt >= :fromDate
    GROUP BY rl.recommendedProductId
    ORDER BY COUNT(DISTINCT rl.id) DESC
    """)
    List<Object[]> getProductRecommendationStats(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
