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
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.RecommendationConfigRepository;
import vn.edu.iuh.fit.services.RecommendationConfigService;

import java.util.List;
import java.util.stream.Collectors;

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
        RecommendationConfig config = configRepository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("Config not found: " + key));

        config.setValue(value);
        configRepository.save(config);

        // Notify the Python recommendation service to reload its configuration
        reloadPythonRecommendationConfig();

        return new RecommendationConfigResponse(config.getKey(), config.getValue(), config.getDescription());
    }

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
