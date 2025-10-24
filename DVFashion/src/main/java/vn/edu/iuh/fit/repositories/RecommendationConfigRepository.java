/*
 * @ {#} RecommendationConfigRepository.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.RecommendationConfig;

import java.util.Optional;

/*
 * @description: Repository interface for managing recommendation configurations
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Repository
public interface RecommendationConfigRepository extends JpaRepository<RecommendationConfig, Long> {
    /**
     * Finds a recommendation configuration by its key.
     *
     * @param key The key of the recommendation configuration.
     * @return An Optional containing the found RecommendationConfig, or empty if not found.
     */
    Optional<RecommendationConfig> findByKey(String key);
}
