/*
 * @ {#} PromotionRepository.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Promotion;

/*
 * @description: Repository interface for managing Promotion entities
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
