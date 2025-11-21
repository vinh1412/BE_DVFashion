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
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.RecommendationLogRepository;
import vn.edu.iuh.fit.repositories.UserProductInteractionRepository;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.services.RecommendationService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public List<ProductResponse> getTodayRecommendations(Long userId, int limit) {
        try {
            if (userId == null) {
                // User not logged in - return top popular products
                return getTopPopularProducts(limit);
            }

            // User logged in - get personalized recommendations
            return getPersonalizedTodayRecommendations(userId, limit);

        } catch (Exception e) {
            log.error("Error getting today's recommendations for user {}: {}", userId, e.getMessage());
            // Fallback to popular products
            return getTopPopularProducts(limit);
        }
    }

    // Get top popular products based on overall interactions
    private List<ProductResponse> getTopPopularProducts(int limit) {
        try {
            Language language = LanguageUtils.getCurrentLanguage();
            Pageable pageable = PageRequest.of(0, limit);

            // Get products ordered by interaction count (view, cart, purchase)
            List<Object[]> popularProducts = userProductInteractionRepository
                    .findTopProductsByInteractionCount(pageable);

            if (popularProducts.isEmpty()) {
                // Fallback to newest products if no interaction data
                return productRepository.findByStatusOrderByCreatedAtDesc(
                                ProductStatus.ACTIVE, pageable)
                        .stream()
                        .map(product -> productService.getProductById(product.getId(), language))
                        .collect(Collectors.toList());
            }

            return popularProducts.stream()
                    .map(result -> {
                        Long productId = (Long) result[0];
                        try {
                            return productService.getProductById(productId, language);
                        } catch (Exception e) {
                            log.warn("Product {} not found: {}", productId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting top popular products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // Get personalized recommendations for logged-in user
    private List<ProductResponse> getPersonalizedTodayRecommendations(Long userId, int limit) {
        try {
            // Verify user exists
            userService.findById(userId);

            // Strategy 1: Get recommendations based on user's recent interactions
            List<ProductResponse> recentInteractionBasedRecs = getRecommendationsFromRecentInteractions(userId, limit);

            if (!recentInteractionBasedRecs.isEmpty()) {
                return recentInteractionBasedRecs;
            }

            // Strategy 2: Get recommendations based on user's purchase history
            List<ProductResponse> purchaseHistoryBasedRecs = getRecommendationsFromPurchaseHistory(userId, limit);

            if (!purchaseHistoryBasedRecs.isEmpty()) {
                return purchaseHistoryBasedRecs;
            }

            // Strategy 3: Get recommendations based on user's category preferences
            List<ProductResponse> categoryBasedRecs = getRecommendationsFromCategoryPreferences(userId, limit);

            if (!categoryBasedRecs.isEmpty()) {
                return categoryBasedRecs;
            }

            // Fallback: Get hybrid recommendations with user's most viewed product
            Long mostViewedProductId = getMostViewedProductByUser(userId);
            if (mostViewedProductId != null) {
                return getHybridRecommendations(userId, mostViewedProductId, limit);
            }

            // Final fallback: Popular products
            return getTopPopularProducts(limit);

        } catch (Exception e) {
            log.error("Error getting personalized recommendations for user {}: {}", userId, e.getMessage());
            return getTopPopularProducts(limit);
        }
    }

    // Recommendation strategies
    private List<ProductResponse> getRecommendationsFromRecentInteractions(Long userId, int limit) {
        try {
            // Get user's recent interactions (last 7 days)
            LocalDateTime recentDate = LocalDateTime.now().minusDays(7);
            Pageable pageable = PageRequest.of(0, 5); // Get top 5 recent interactions

            List<Object[]> recentInteractions = userProductInteractionRepository
                    .findRecentInteractionsByUser(userId, recentDate, pageable);

            if (recentInteractions.isEmpty()) {
                return Collections.emptyList();
            }

            Language language = LanguageUtils.getCurrentLanguage();
            List<ProductResponse> recommendations = new ArrayList<>();

            // For each recent interaction, get similar products
            for (Object[] interaction : recentInteractions) {
                Long productId = (Long) interaction[0];

                try {
                    // Get recommendations for this product (content-based)
                    List<ProductResponse> productRecs = getRecommendations(productId, 2);
                    recommendations.addAll(productRecs);

                    if (recommendations.size() >= limit) {
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to get recommendations for product {}: {}", productId, e.getMessage());
                }
            }

            // Remove duplicates and limit results
            return recommendations.stream()
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting recommendations from recent interactions for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Get recommendations based on user's purchase history
    private List<ProductResponse> getRecommendationsFromPurchaseHistory(Long userId, int limit) {
        try {
            // Get user's purchase history (last 30 days)
            LocalDateTime recentDate = LocalDateTime.now().minusDays(30);
            Pageable pageable = PageRequest.of(0, 3);

            List<Object[]> purchaseHistory = userProductInteractionRepository
                    .findPurchaseHistoryByUser(userId, recentDate, pageable);

            if (purchaseHistory.isEmpty()) {
                return Collections.emptyList();
            }

            Language language = LanguageUtils.getCurrentLanguage();
            List<ProductResponse> recommendations = new ArrayList<>();

            // For each purchased product, get similar products
            for (Object[] purchase : purchaseHistory) {
                Long productId = (Long) purchase[0];

                try {
                    List<ProductResponse> productRecs = getRecommendations(productId, 3);
                    recommendations.addAll(productRecs);

                    if (recommendations.size() >= limit) {
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to get recommendations for purchased product {}: {}", productId, e.getMessage());
                }
            }

            return recommendations.stream()
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting recommendations from purchase history for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Get recommendations based on user's preferred categories
    private List<ProductResponse> getRecommendationsFromCategoryPreferences(Long userId, int limit) {
        try {
            // Get user's preferred categories based on interactions
            Pageable pageable = PageRequest.of(0, 3);
            List<Object[]> preferredCategories = userProductInteractionRepository
                    .findPreferredCategoriesByUser(userId, pageable);

            if (preferredCategories.isEmpty()) {
                return Collections.emptyList();
            }

            Language language = LanguageUtils.getCurrentLanguage();
            List<ProductResponse> recommendations = new ArrayList<>();

            // Get products from preferred categories
            for (Object[] categoryData : preferredCategories) {
                Long categoryId = (Long) categoryData[0];

                try {
                    List<Product> categoryProducts = productRepository
                            .findTopProductsByCategory(categoryId, ProductStatus.ACTIVE, PageRequest.of(0, 3));

                    List<ProductResponse> categoryRecs = categoryProducts.stream()
                            .map(product -> productService.getProductById(product.getId(), language))
                            .collect(Collectors.toList());

                    recommendations.addAll(categoryRecs);

                    if (recommendations.size() >= limit) {
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to get products for category {}: {}", categoryId, e.getMessage());
                }
            }

            return recommendations.stream()
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting recommendations from category preferences for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Get the most viewed product by the user
    private Long getMostViewedProductByUser(Long userId) {
        try {
            List<Object[]> mostViewed = userProductInteractionRepository
                    .findMostViewedProductByUser(userId, PageRequest.of(0, 1));

            return mostViewed.isEmpty() ? null : (Long) mostViewed.get(0)[0];
        } catch (Exception e) {
            log.error("Error getting most viewed product for user {}: {}", userId, e.getMessage());
            return null;
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
