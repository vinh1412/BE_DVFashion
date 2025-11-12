/*
 * @ {#} PromotionRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Promotion;
import vn.edu.iuh.fit.enums.Language;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Repository interface for managing Promotion entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    /**
     * Finds all active promotions that are currently valid based on the current date and time.
     *
     * @param now The current date and time.
     * @return A list of active promotions.
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startDate <= :now AND p.endDate >= :now ORDER BY p.startDate DESC")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Finds all active promotions that are currently valid based on the current date and time, with pagination support.
     *
     * @param now The current date and time.
     * @param pageable The pagination information.
     * @return A page of active promotions.
     */
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startDate <= :now AND p.endDate >= :now")
    Page<Promotion> findActivePromotions(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Retrieves the top promotions based on total revenue generated from sold products.
     * The results are ordered by total revenue in descending order.
     *
     * @param language the language for promotion names
     * @param pageable pagination information to limit the number of results
     * @return a list of objects containing promotion ID, name, and total revenue
     */
    @Query("""
        SELECT p.id AS promotionId,
               pt.name AS promotionName, 
               SUM(pp.promotionPrice * pp.soldQuantity) AS totalRevenue
        FROM Promotion p
        JOIN p.promotionProducts pp
        JOIN p.translations pt            
        WHERE pt.language = :language      
        GROUP BY p.id, pt.name
        ORDER BY totalRevenue DESC
    """)
    List<Object[]> findTopPromotionsByRevenue(
            @Param("language") Language language,
            Pageable pageable
    );

    /**
     * Counts the total number of promotions.
     *
     * @return the total number of promotions
     */
    @Query("SELECT COUNT(p) FROM Promotion p")
    long countTotalPromotions();

    /**
     * Counts the number of promotions by their active status.
     *
     * @param active the active status to filter by
     * @return the number of promotions with the specified active status
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.active = :active")
    long countPromotionsByActiveStatus(@Param("active") boolean active);

    /**
     * Counts the number of promotions grouped by their active status.
     *
     * @return a list of Object arrays, each containing the active status and the corresponding count
     */
    @Query("SELECT p.active, COUNT(p) FROM Promotion p GROUP BY p.active")
    List<Object[]> countPromotionsByAllActiveStatuses();

    /**
     * Counts the number of expired promotions.
     *
     * @return the number of expired promotions
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.endDate < CURRENT_TIMESTAMP")
    long countExpiredPromotions();

    /**
     * Counts the number of currently active promotions.
     *
     * @return the number of currently active promotions
     */
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.startDate <= CURRENT_TIMESTAMP AND p.endDate >= CURRENT_TIMESTAMP AND p.active = true")
    long countCurrentlyActivePromotions();
}
