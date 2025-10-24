/*
 * @ {#} UserProductInteractionRepository.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;

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
}
