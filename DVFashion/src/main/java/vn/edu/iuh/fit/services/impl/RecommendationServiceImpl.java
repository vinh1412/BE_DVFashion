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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.request.HybridRecommendationRequest;
import vn.edu.iuh.fit.dtos.request.RecommendationRequest;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.RecommendationLog;
import vn.edu.iuh.fit.enums.InteractionType;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.RecommendationLogRepository;
import vn.edu.iuh.fit.repositories.UserProductInteractionRepository;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.services.RecommendationService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    private final RecommendationLogRepository recommendationLogRepository;

    private final UserProductInteractionRepository userProductInteractionRepository;

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

            // Log each suggested product
            if (recommendations != null && !recommendations.isEmpty()) {
                recommendations.forEach(rec -> {
                    try {
                        RecommendationLog logEntry = RecommendationLog.builder()
                                .userId(userId)
                                .productId(productId)
                                .recommendedProductId(rec.productId())
                                .build();

                        recommendationLogRepository.save(logEntry);
                    } catch (Exception ex) {
                        log.error("Failed to save recommendation log for product {}: {}", rec.productId(), ex.getMessage());
                    }
                });
            }

            return recommendations.stream()
                    .map(rec -> productService.getProductById(rec.productId(), language))
                    .toList();

        } catch (Exception e) {
            log.error("Error getting hybrid recommendations: {}", e.getMessage());
            return getFallbackRecommendations(productId, numRecommendations);
        }
    }

    @Override
    public List<TopRecommendedProductResponse> getTopRecommendedProducts(int limit, Integer days) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> results;

            if (days != null && days > 0) {
                LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
                results = recommendationLogRepository.findTopRecommendedProducts(fromDate, pageable);
            } else {
                results = recommendationLogRepository.findTopRecommendedProductsAllTime(pageable);
            }

            Language language = LanguageUtils.getCurrentLanguage();

            return results.stream()
                    .map(result -> {
                        Long productId = (Long) result[0];
                        Long count = (Long) result[1];

                        try {
                            ProductResponse product = productService.getProductById(productId, language);
                            return TopRecommendedProductResponse.builder()
                                    .productId(productId)
                                    .productName(product.name())
                                    .categoryName(product.categoryName())
                                    .recommendationCount(count)
                                    .averagePrice(Double.parseDouble(product.price().toString()))
                                    .build();
                        } catch (Exception e) {
                            log.warn("Product {} not found or error occurred: {}", productId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting top recommended products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public RecommendationAnalyticsResponse getRecommendationAnalytics(Integer days) {
        try {
            LocalDateTime fromDate = days != null ? LocalDateTime.now().minusDays(days) : null;

            // Get total recommendations
            Long totalRecommendations = fromDate != null
                    ? recommendationLogRepository.countTotalRecommendations(fromDate)
                    : recommendationLogRepository.countTotalRecommendationsAllTime();

            // Get interaction counts
            Long totalClicks = fromDate != null
                    ? userProductInteractionRepository.countInteractionsByTypeAfterRecommendation(
                    InteractionType.VIEW, fromDate)
                    : userProductInteractionRepository.countInteractionsByTypeAfterRecommendationAllTime(
                    InteractionType.VIEW);

            Long totalAddToCarts = fromDate != null
                    ? userProductInteractionRepository.countInteractionsByTypeAfterRecommendation(
                    InteractionType.ADD_TO_CART, fromDate)
                    : userProductInteractionRepository.countInteractionsByTypeAfterRecommendationAllTime(
                    InteractionType.ADD_TO_CART);

            Long totalPurchases = fromDate != null
                    ? userProductInteractionRepository.countInteractionsByTypeAfterRecommendation(
                    InteractionType.PURCHASE, fromDate)
                    : userProductInteractionRepository.countInteractionsByTypeAfterRecommendationAllTime(
                    InteractionType.PURCHASE);

            // Calculate rates
            double clickThroughRate = totalRecommendations > 0
                    ? (double) totalClicks / totalRecommendations * 100 : 0.0;

            double cartConversionRate = totalClicks > 0
                    ? (double) totalAddToCarts / totalClicks * 100 : 0.0;

            double purchaseConversionRate = totalAddToCarts > 0
                    ? (double) totalPurchases / totalAddToCarts * 100 : 0.0;

            String period = days != null ? days + " days" : "All time";

            return RecommendationAnalyticsResponse.builder()
                    .totalRecommendations(totalRecommendations)
                    .totalClicks(totalClicks)
                    .totalAddToCarts(totalAddToCarts)
                    .totalPurchases(totalPurchases)
                    .clickThroughRate(Math.round(clickThroughRate * 100.0) / 100.0)
                    .cartConversionRate(Math.round(cartConversionRate * 100.0) / 100.0)
                    .purchaseConversionRate(Math.round(purchaseConversionRate * 100.0) / 100.0)
                    .period(period)
                    .build();

        } catch (Exception e) {
            log.error("Error getting recommendation analytics: {}", e.getMessage());
            return RecommendationAnalyticsResponse.builder()
                    .totalRecommendations(0L)
                    .totalClicks(0L)
                    .totalAddToCarts(0L)
                    .totalPurchases(0L)
                    .clickThroughRate(0.0)
                    .cartConversionRate(0.0)
                    .purchaseConversionRate(0.0)
                    .period(days != null ? days + " days" : "All time")
                    .build();
        }
    }

    @Override
    public List<ProductRecommendationStatsResponse> getProductRecommendationStats(int limit, Integer days) {
        try {
            LocalDateTime fromDate = days != null
                    ? LocalDateTime.now().minusDays(days)
                    : LocalDateTime.now().minusYears(10); // Very old date for all time
            LocalDateTime endDate = LocalDateTime.now();

            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> results = recommendationLogRepository.getProductRecommendationStats(
                    fromDate, endDate, pageable
            );

            Language language = LanguageUtils.getCurrentLanguage();

            return results.stream()
                    .map(result -> {
                        Long productId = (Long) result[0];
                        Long recommendationCount = (Long) result[1];
                        Long clickCount = result[2] != null ? (Long) result[2] : 0L;
                        Long addToCartCount = result[3] != null ? (Long) result[3] : 0L;
                        Long purchaseCount = result[4] != null ? (Long) result[4] : 0L;

                        try {
                            ProductResponse product = productService.getProductById(productId, language);

                            double clickThroughRate = recommendationCount > 0
                                    ? (double) clickCount / recommendationCount * 100 : 0.0;
                            double cartConversionRate = clickCount > 0
                                    ? (double) addToCartCount / clickCount * 100 : 0.0;
                            double purchaseConversionRate = addToCartCount > 0
                                    ? (double) purchaseCount / addToCartCount * 100 : 0.0;

                            return ProductRecommendationStatsResponse.builder()
                                    .productId(productId)
                                    .productName(product.name())
                                    .categoryName(product.categoryName())
                                    .recommendationCount(recommendationCount)
                                    .clickCount(clickCount)
                                    .addToCartCount(addToCartCount)
                                    .purchaseCount(purchaseCount)
                                    .clickThroughRate(Math.round(clickThroughRate * 100.0) / 100.0)
                                    .cartConversionRate(Math.round(cartConversionRate * 100.0) / 100.0)
                                    .purchaseConversionRate(Math.round(purchaseConversionRate * 100.0) / 100.0)
                                    .build();
                        } catch (Exception e) {
                            log.warn("Product {} not found: {}", productId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting product recommendation stats: {}", e.getMessage());
            return Collections.emptyList();
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
