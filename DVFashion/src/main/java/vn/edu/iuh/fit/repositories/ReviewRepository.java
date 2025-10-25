/*
 * @ {#} ReviewRepository.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Review;
import vn.edu.iuh.fit.enums.ReviewStatus;

import java.util.List;

/*
 * @description: Repository interface for managing product reviews.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    /**
     * Checks if a review exists for a specific user, order, and product variant.
     *
     * @param userId           the ID of the user
     * @param orderId          the ID of the order
     * @param productVariantId the ID of the product variant
     * @return true if such a review exists, false otherwise
     */
    boolean existsByUserIdAndOrderIdAndProductVariantId(Long userId, Long orderId, Long productVariantId);

    /**
     * Retrieves a list of reviews for a specific product filtered by review statuses.
     *
     * @param productId the ID of the product
     * @param statuses  the list of review statuses to filter by
     * @return a list of matching reviews
     */
    @Query("SELECT r FROM Review r " +
            "WHERE r.productVariant.product.id = :productId " +
            "AND r.status IN :statuses")
    List<Review> findByProductIdAndStatusIn(
            @Param("productId") Long productId,
            @Param("statuses") List<ReviewStatus> statuses);

    /**
     * Retrieves all reviews with optional filtering by status and sorting.
     * @param status the review status to filter by (nullable)
     * @param sort   the sorting criteria
     * @return a list of reviews matching the filter and sorted accordingly
     */
    @Query("SELECT r FROM Review r " +
            "WHERE (:status IS NULL OR r.status = :status)")
    List<Review> findAllWithFilter(
            @Param("status") ReviewStatus status,
            Sort sort);

    /**
     * Counts the total number of reviews in the database.
     *
     * @return the total count of reviews
     */
    @Query("SELECT COUNT(r) FROM Review r")
    long countAllReviews();

    /**
     * Counts the number of reviews grouped by their status.
     *
     * @return a list of objects where each object contains a status and its corresponding count
     */
    @Query("SELECT r.status, COUNT(r) FROM Review r GROUP BY r.status")
    List<Object[]> countReviewsByStatus();

    /**
     * Counts the number of reviews for a specific product grouped by rating.
     *
     * @param productId the ID of the product
     * @param statuses  the list of review statuses to filter by
     * @return a list of objects where each object contains a rating and its corresponding count
     */
    @Query("""
        SELECT r.rating, COUNT(r) 
        FROM Review r 
        WHERE r.productVariant.product.id = :productId 
        AND r.status IN :statuses 
        GROUP BY r.rating
        """)
    List<Object[]> countReviewsByRating(
            @Param("productId") Long productId,
            @Param("statuses") List<ReviewStatus> statuses
    );

    /**
     * Calculates the average rating for a specific product based on reviews with specified statuses.
     *
     * @param productId the ID of the product
     * @param statuses  the list of review statuses to filter by
     * @return the average rating, or null if there are no matching reviews
     */
    @Query("""
        SELECT AVG(r.rating) 
        FROM Review r 
        WHERE r.productVariant.product.id = :productId 
        AND r.status IN :statuses
        """)
    Double getAverageRating(
            @Param("productId") Long productId,
            @Param("statuses") List<ReviewStatus> statuses
    );

    /**
     * Counts the number of reviews for a specific product that include images, filtered by review statuses.
     *
     * @param productId the ID of the product
     * @param statuses  the list of review statuses to filter by
     * @return the count of reviews with images
     */
    @Query("""
        SELECT COUNT(DISTINCT r.id) 
        FROM Review r 
        JOIN r.images img 
        WHERE r.productVariant.product.id = :productId 
        AND r.status IN :statuses
        """)
    long countReviewsWithImages(
            @Param("productId") Long productId,
            @Param("statuses") List<ReviewStatus> statuses
    );

    /**
     * Counts the number of reviews for a specific product that include comments, filtered by review statuses.
     *
     * @param productId the ID of the product
     * @param statuses  the list of review statuses to filter by
     * @return the count of reviews with comments
     */
    @Query("""
        SELECT COUNT(DISTINCT r.id) 
        FROM Review r 
        JOIN r.translations t 
        WHERE r.productVariant.product.id = :productId 
        AND r.status IN :statuses 
        AND t.comment IS NOT NULL 
        AND t.comment != ''
        """)
    long countReviewsWithComments(
            @Param("productId") Long productId,
            @Param("statuses") List<ReviewStatus> statuses
    );
}
