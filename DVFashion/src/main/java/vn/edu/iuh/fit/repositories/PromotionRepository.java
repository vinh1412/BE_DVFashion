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
}
