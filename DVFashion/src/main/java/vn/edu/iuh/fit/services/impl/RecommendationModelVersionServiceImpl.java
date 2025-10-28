/*
 * @ {#} RecommendationModelVersionServiceImpl.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.request.CreateRecommendationModelVersionRequest;
import vn.edu.iuh.fit.dtos.response.PythonEvaluationResponse;
import vn.edu.iuh.fit.dtos.response.RecommendationModelVersionResponse;
import vn.edu.iuh.fit.entities.RecommendationModelVersion;
import vn.edu.iuh.fit.exceptions.NotActiveException;
import vn.edu.iuh.fit.mappers.RecommendationModelVersionMapper;
import vn.edu.iuh.fit.repositories.RecommendationModelVersionRepository;
import vn.edu.iuh.fit.services.RecommendationModelVersionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * @description: Service implementation for managing recommendation model versions
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationModelVersionServiceImpl implements RecommendationModelVersionService {
    private final RecommendationModelVersionRepository repository;

    private final RestTemplate restTemplate;

    private final RecommendationModelVersionMapper mapper;

    @Value("#{ '${recommendation.service.url}' + '${web.base-path}' }")
    private String recommendationServiceUrl;

    @Override
    public List<RecommendationModelVersionResponse> getAllVersions() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void activateModel(Long id) {
        repository.findAll().forEach(model -> {
            model.setIsActive(model.getId().equals(id));
            repository.save(model);
        });
        log.info("Activated model version id={}", id);

        // Call Python service to reload config
        try {
            String url = recommendationServiceUrl + "/reload-config";
            restTemplate.postForObject(url, null, String.class);
            log.info("Reloaded config in Python service.");
        } catch (Exception e) {
            log.error("Failed to reload config: {}", e.getMessage());
        }
    }

    @Override
    public RecommendationModelVersionResponse getActiveModel() {
        return repository.findByIsActiveTrue()
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotActiveException("No active recommendation model found."));
    }

    @Override
    public RecommendationModelVersionResponse evaluateModel(CreateRecommendationModelVersionRequest request) {
        try {
            // Gửi request sang Python /evaluate
            String url = recommendationServiceUrl + "/evaluate";
            log.info("Evaluating model '{}' via Python API: {}", request.modelName(), url);

            // Gửi request sang Python
            Map<String, String> body = Map.of("model_name", request.modelName());
            PythonEvaluationResponse response = restTemplate.postForObject(url, body, PythonEvaluationResponse.class);
            if (response == null || response.getMetrics() == null) {
                throw new RuntimeException("No evaluation metrics returned from Python");
            }

            var metrics = response.getMetrics();

            // Save new model version
            RecommendationModelVersion entity = new RecommendationModelVersion();
            entity.setModelName(metrics.getModel_name());
            entity.setContentWeight(metrics.getContent_weight());
            entity.setCollaborativeWeight(metrics.getCollaborative_weight());
            entity.setPrecisionAt10(metrics.getPrecision_at_10());
            entity.setRecallAt10(metrics.getRecall_at_10());
            entity.setMapAt10(metrics.getMap_at_10());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setIsActive(false);

            repository.save(entity);

            log.info("Evaluated and saved model version '{}'", entity.getModelName());
            return mapper.toResponse(entity);
        } catch (Exception e) {
            log.error("Failed to evaluate model: {}", e.getMessage());
            throw new RuntimeException("Failed to evaluate model: " + e.getMessage());
        }
    }
}
