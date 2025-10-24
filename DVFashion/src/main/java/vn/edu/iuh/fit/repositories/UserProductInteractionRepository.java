/*
 * @ {#} UserProductInteractionRepository.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;

import java.time.LocalDateTime;
import java.util.Optional;

/*
 * @description: Repository interface for managing user-product interactions
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Repository
public interface UserProductInteractionRepository extends JpaRepository<UserProductInteraction, Long> {
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
}
