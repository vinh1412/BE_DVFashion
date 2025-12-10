/*
 * @ {#} UserProductInteractionRepository.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing user-product interactions
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Repository
public interface UserProductInteractionRepository extends JpaRepository<UserProductInteraction, Long>, JpaSpecificationExecutor<UserProductInteraction> {
    /**
     * Finds a user-product interaction by user ID, product ID, and interaction type.
     *
     * @param userId          The ID of the user.
     * @param productId       The ID of the product.
     * @param interactionType The type of interaction.
     * @return An Optional containing the found UserProductInteraction, or empty if not found.
     */
    Optional<UserProductInteraction> findByUser_IdAndProduct_IdAndInteractionType(
            Long userId,
            Long productId,
            InteractionType interactionType
    );

    /**
     * Counts the number of distinct user-product interactions of a specific type
     * that occurred after a recommendation was made, since a given date.
     *
     * @param interactionType The type of interaction to count.
     * @param fromDate        The date from which to start counting.
     * @return The count of distinct interactions.
     */
    @Query("""
            SELECT COUNT(DISTINCT upi.id)
            FROM UserProductInteraction upi
            WHERE upi.interactionType = :interactionType
            AND upi.createdAt >= :fromDate
            AND EXISTS (
                SELECT 1 FROM RecommendationLog rl 
                WHERE rl.recommendedProductId = upi.product.id 
                AND rl.userId = upi.user.id
                AND rl.createdAt <= upi.createdAt
            )
            """)
    Long countInteractionsByTypeAfterRecommendation(
            @Param("interactionType") InteractionType interactionType,
            @Param("fromDate") LocalDateTime fromDate
    );

    /**
     * Counts the number of distinct user-product interactions of a specific type
     * that occurred after a recommendation was made, for all time.
     *
     * @param interactionType The type of interaction to count.
     * @return The count of distinct interactions.
     */
    @Query("""
            SELECT COUNT(DISTINCT upi.id)
            FROM UserProductInteraction upi
            WHERE upi.interactionType = :interactionType
            AND EXISTS (
                SELECT 1 FROM RecommendationLog rl 
                WHERE rl.recommendedProductId = upi.product.id 
                AND rl.userId = upi.user.id
                AND rl.createdAt <= upi.createdAt
            )
            """)
    Long countInteractionsByTypeAfterRecommendationAllTime(
            @Param("interactionType") InteractionType interactionType
    );

    /**
     * Finds the top products by interaction count.
     *
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing product ID and interaction count.
     */
    @Query("""
                SELECT p.id, COUNT(upi.id) as interactionCount
                FROM UserProductInteraction upi
                JOIN upi.product p
                WHERE p.status = 'ACTIVE'
                GROUP BY p.id
                ORDER BY interactionCount DESC
            """)
    List<Object[]> findTopProductsByInteractionCount(Pageable pageable);

    /**
     * Finds recent interactions by a user since a given date.
     *
     * @param userId   The ID of the user.
     * @param fromDate The date from which to find interactions.
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing product ID, interaction type, and the latest interaction date.
     */
    @Query("""
                SELECT upi.product.id, upi.interactionType, MAX(upi.createdAt)
                FROM UserProductInteraction upi
                WHERE upi.user.id = :userId
                AND upi.createdAt >= :fromDate
                GROUP BY upi.product.id, upi.interactionType
                ORDER BY MAX(upi.createdAt) DESC
            """)
    List<Object[]>
    findRecentInteractionsByUser(@Param("userId") Long userId,
                                 @Param("fromDate") LocalDateTime fromDate,
                                 Pageable pageable);

    /**
     * Finds purchase history by a user since a given date.
     *
     * @param userId   The ID of the user.
     * @param fromDate The date from which to find purchase history.
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing product ID and purchase count.
     */
    @Query("""
                SELECT upi.product.id, COUNT(upi.id) as purchaseCount
                FROM UserProductInteraction upi
                WHERE upi.user.id = :userId
                AND upi.interactionType = 'PURCHASE'
                AND upi.createdAt >= :fromDate
                GROUP BY upi.product.id
                ORDER BY purchaseCount DESC, MAX(upi.createdAt) DESC
            """)
    List<Object[]> findPurchaseHistoryByUser(@Param("userId") Long userId,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             Pageable pageable);

    /**
     * Finds preferred categories by a user based on interaction count.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing category ID and interaction count.
     */
    @Query("""
                SELECT p.category.id, COUNT(upi.id) as interactionCount
                FROM UserProductInteraction upi
                JOIN upi.product p
                WHERE upi.user.id = :userId
                GROUP BY p.category.id
                ORDER BY interactionCount DESC
            """)
    List<Object[]> findPreferredCategoriesByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds the most viewed products by a user.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing product ID and view count.
     */
    @Query("""
                SELECT upi.product.id, COUNT(upi.id) as viewCount
                FROM UserProductInteraction upi
                WHERE upi.user.id = :userId
                AND upi.interactionType = 'VIEW'
                GROUP BY upi.product.id
                ORDER BY viewCount DESC
            """)
    List<Object[]> findMostViewedProductByUser(@Param("userId") Long userId, Pageable pageable);

   /**
     * Finds today's interactions by a user.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination information.
     * @return A list of object arrays, each containing product ID, interaction type, and the latest interaction date.
     */
    @Query("""
        SELECT DISTINCT upi.product.id, upi.interactionType, MAX(upi.createdAt) as latestInteraction
        FROM UserProductInteraction upi
        WHERE upi.user.id = :userId
          AND upi.createdAt BETWEEN :startOfDay AND :endOfDay
        GROUP BY upi.product.id, upi.interactionType
        ORDER BY MAX(upi.createdAt) DESC
    """)
    List<Object[]> findTodayInteractionsByUser(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable
    );

    /**
     * Finds today's interactions by a user filtered by interaction type.
     *
     * @param userId          The ID of the user.
     * @param interactionType The type of interaction.
     * @param pageable        Pagination information.
     * @return A list of object arrays, each containing product ID and the latest interaction date.
     */
    @Query("""
        SELECT DISTINCT upi.product.id, MAX(upi.createdAt) as latestInteraction
        FROM UserProductInteraction upi
        WHERE upi.user.id = :userId
          AND upi.interactionType = :interactionType
          AND upi.createdAt BETWEEN :startOfDay AND :endOfDay
        GROUP BY upi.product.id
        ORDER BY MAX(upi.createdAt) DESC
    """)
    List<Object[]> findTodayInteractionsByUserAndType(
            @Param("userId") Long userId,
            @Param("interactionType") InteractionType interactionType,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable
    );
}
