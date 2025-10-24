/*
 * @ {#} RecommendationServiceImpl.java   1.0     20/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.request.HybridRecommendationRequest;
import vn.edu.iuh.fit.dtos.request.RecommendationRequest;
import vn.edu.iuh.fit.dtos.request.UserInteractionRequest;
import vn.edu.iuh.fit.dtos.response.ProductRecommendationResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.services.RecommendationService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for managing product recommendations
 * @author: Tran Hien Vinh
 * @date:   20/09/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    @Value("#{ '${recommendation.service.url}' + '${web.base-path}' }")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;

    private final ProductService productService;

    private final ProductRepository productRepository;

    private final UserService userService;

    @Override
    public List<ProductResponse> getRecommendations(Long productId, int numRecommendations) {
        try {
            RecommendationRequest request = new RecommendationRequest(productId, numRecommendations);

            // Verify product exists
            productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));


            // Call api recommendation from python service
            ResponseEntity<List<ProductRecommendationResponse>> response = restTemplate.exchange(
                    recommendationServiceUrl + "/recommendations",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<List<ProductRecommendationResponse>>() {
                    }
            );

            // Get recommendations from response
            List<ProductRecommendationResponse> recommendations = response.getBody();

            // Get current language
            Language language = LanguageUtils.getCurrentLanguage();

            // Fetch product details for each recommended product
            return recommendations.stream()
                    .map(rec -> productService.getProductById(rec.productId(), language))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting recommendations for product {}: {}", productId, e.getMessage());
            // Fallback strategy
            return getFallbackRecommendations(productId, numRecommendations);
        }
    }

    @Override
    public List<ProductResponse> getHybridRecommendations(Long userId, Long productId, int numRecommendations) {
        if (userId != null) {
            // Verify user exists
            userService.findById(userId);
        }

       if (productId != null) {
            // Verify product exists
            productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        }
        try {
            HybridRecommendationRequest request = HybridRecommendationRequest.builder()
                    .userId(userId) // can be null
                    .productId(productId)
                    .numRecommendations(numRecommendations)
                    .useCollaborative(true)
                    .build();

            ResponseEntity<List<ProductRecommendationResponse>> response = restTemplate.exchange(
                    recommendationServiceUrl + "/recommendations",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<List<ProductRecommendationResponse>>() {}
            );

            List<ProductRecommendationResponse> recommendations = response.getBody();
            Language language = LanguageUtils.getCurrentLanguage();

            return recommendations.stream()
                    .map(rec -> productService.getProductById(rec.productId(), language))
                    .toList();

        } catch (Exception e) {
            log.error("Error getting hybrid recommendations: {}", e.getMessage());
            return getFallbackRecommendations(productId, numRecommendations);
        }
    }

    // Fallback method to get recommendations based on category
    private List<ProductResponse> getFallbackRecommendations(Long productId, int numRecommendations) {
        try {
            // Get the original product to find similar products
            Product originalProduct = productRepository.findById(productId).orElse(null);
            if (originalProduct == null) {
                return Collections.emptyList();
            }

            Language language = LanguageUtils.getCurrentLanguage();

            // Find products from the same category
            List<ProductResponse> recommendations = productRepository
                    .findByCategoryIdAndIdNotAndStatus(
                            originalProduct.getCategory().getId(),
                            productId,
                            originalProduct.getStatus()
                    )
                    .stream()
                    .limit(numRecommendations)
                    .map(product -> productService.getProductById(product.getId(), language))
                    .collect(Collectors.toList());

            // If still not enough, add random popular products
            if (recommendations.size() < numRecommendations) {
                int remaining = numRecommendations - recommendations.size();

                List<Long> existingIds = recommendations.stream()
                        .map(ProductResponse::id)
                        .collect(Collectors.toList());
                existingIds.add(productId);

                List<ProductResponse> randomRecommendations = productRepository
                        .findByIdNotInAndStatusOrderByCreatedAtDesc(
                                existingIds,
                                originalProduct.getStatus()
                        )
                        .stream()
                        .limit(remaining)
                        .map(product -> productService.getProductById(product.getId(), language))
                        .collect(Collectors.toList());

                recommendations.addAll(randomRecommendations);
            }

            return recommendations;

        } catch (Exception e) {
            log.error("Error in fallback recommendations for product {}: {}", productId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
