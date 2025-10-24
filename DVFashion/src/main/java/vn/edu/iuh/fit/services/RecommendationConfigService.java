/*
 * @ {#} RecommendationConfigService.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.RecommendationConfigResponse;

import java.util.List;

/*
 * @description: Service interface for managing recommendation configurations
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
public interface RecommendationConfigService {
    /**
     * Retrieves all recommendation configurations.
     *
     * @return A list of RecommendationConfigResponse objects.
     */
    List<RecommendationConfigResponse> getAllConfigs();

    /**
     * Updates a recommendation configuration by its key.
     *
     * @param key   The key of the recommendation configuration to update.
     * @param value The new value for the recommendation configuration.
     * @return The updated RecommendationConfigResponse object.
     */
    RecommendationConfigResponse updateConfig(String key, String value);
}
