/*
 * @ {#} RecommendationModelVersionRepository.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.entities.RecommendationModelVersion;

import java.util.Optional;

/*
 * @description: Repository interface for RecommendationModelVersion entity
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
public interface RecommendationModelVersionRepository extends JpaRepository<RecommendationModelVersion, Long> {
    /*
     * Finds the active recommendation model version.
     *
     * @return an Optional containing the active RecommendationModelVersion if found, otherwise empty
     */
    Optional<RecommendationModelVersion> findByIsActiveTrue();
    Optional<RecommendationModelVersion> findTopByOrderByCreatedAtDesc();
}
