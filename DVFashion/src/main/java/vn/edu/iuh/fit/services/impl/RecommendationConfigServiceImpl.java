/*
 * @ {#} RecommendationConfigServiceImpl.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.response.RecommendationConfigResponse;
import vn.edu.iuh.fit.entities.RecommendationConfig;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.RecommendationConfigRepository;
import vn.edu.iuh.fit.services.RecommendationConfigService;

import java.util.List;

/*
 * @description: Service implementation for managing recommendation configurations
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationConfigServiceImpl implements RecommendationConfigService {
    @Value("#{ '${recommendation.service.url}' + '${web.base-path}' }")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;

    private final RecommendationConfigRepository configRepository;

    @Override
    public List<RecommendationConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(c -> new RecommendationConfigResponse(c.getKey(), c.getValue(), c.getDescription()))
                .toList();
    }

    @Override
    public RecommendationConfigResponse updateConfig(String key, String value) {
        // Find existing config
        RecommendationConfig config = configRepository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("Config not found: " + key));

        // If updating content_weight or collaborative_weight, adjust the other accordingly
        if (key.equals("content_weight") || key.equals("collaborative_weight")) {
            double newValue = Double.parseDouble(value);

            // Find configs for content and collaborative weights
            RecommendationConfig contentWeightCfg = configRepository.findByKey("content_weight")
                    .orElse(null);
            RecommendationConfig collaborativeWeightCfg = configRepository.findByKey("collaborative_weight")
                    .orElse(null);

            if (key.equals("content_weight") && collaborativeWeightCfg != null) {
                double newCollaborative = Math.max(0, 1.0 - newValue);
                collaborativeWeightCfg.setValue(String.valueOf(newCollaborative));
                configRepository.save(collaborativeWeightCfg);
            }

            if (key.equals("collaborative_weight") && contentWeightCfg != null) {
                double newContent = Math.max(0, 1.0 - newValue);
                contentWeightCfg.setValue(String.valueOf(newContent));
                configRepository.save(contentWeightCfg);
            }

            config.setValue(value);
            configRepository.save(config);

            log.info("Updated hybrid weights: {}={}, adjusted other weight to keep total=1", key, value);
        }

        // If updating min_interaction_count, ensure it's non-negative
        else if (key.equals("min_interaction_count")) {
            int minCount = Integer.parseInt(value);
            if (minCount < 0) {
                throw new BadRequestException("min_interaction_count cannot be negative");
            }

            config.setValue(String.valueOf(minCount));
            configRepository.save(config);

            log.info("Updated config '{}' = {}", key, minCount);
        }

        // Update other configs directly
        else {
            config.setValue(value);
            configRepository.save(config);
        }

        // Notify the Python recommendation service to reload its configuration
        reloadPythonRecommendationConfig();

        return new RecommendationConfigResponse(config.getKey(), config.getValue(), config.getDescription());
    }

    // Helper method to notify Python recommendation service to reload config
    private void reloadPythonRecommendationConfig() {
        try {
            String url = recommendationServiceUrl + "/reload-config";

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(null),
                    String.class
            );
        } catch (Exception e) {
            log.error("Failed to notify Python recommendation service to reload config", e);
        }
    }
}
