/*
 * @ {#} ReviewReplyRepository.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ReviewReply;
import vn.edu.iuh.fit.enums.ReviewReplyStatus;

import java.util.List;

/*
 * @description: Repository interface for managing ReviewReply entities
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    /**
     * Finds top-level review replies for a given review ID and status.
     *
     * @param reviewId the ID of the review
     * @param status   the status of the review replies
     * @return a list of top-level ReviewReply entities
     */
    @Query("""
       SELECT r FROM ReviewReply r
       WHERE r.review.id = :reviewId
         AND r.parentReply IS NULL
         AND r.status = :status
       ORDER BY r.createdAt ASC
       """)
    List<ReviewReply> findTopLevelRepliesByReviewIdAndStatus(
            @Param("reviewId") Long reviewId,
            @Param("status") ReviewReplyStatus status);

    /**
     * Finds child review replies for a given parent reply ID and status.
     * @param parentId the ID of the parent review reply
     * @param status   the status of the review replies
     * @return a list of child ReviewReply entities
     */
    @Query("""
        SELECT rr FROM ReviewReply rr 
        WHERE rr.parentReply.id = :parentId 
          AND rr.status = :status 
        ORDER BY rr.createdAt ASC
    """)
    List<ReviewReply> findChildRepliesByParentId(@Param("parentId") Long parentId, @Param("status") ReviewReplyStatus status);

    /**
     * Finds all top-level review replies for a given review ID (admin view).
     *
     * @param reviewId the ID of the review
     * @return a list of top-level ReviewReply entities
     */
    @Query("""
       SELECT r FROM ReviewReply r
       WHERE r.review.id = :reviewId
       ORDER BY r.createdAt ASC
       """)
    List<ReviewReply> findTopLevelRepliesByReviewIdAdmin(@Param("reviewId") Long reviewId);

    /**
     * Finds all child review replies for a given parent reply ID (admin view).
     * @param parentId the ID of the parent review reply
     * @return a list of child ReviewReply entities
     */
    @Query("""
        SELECT rr FROM ReviewReply rr 
        WHERE rr.parentReply.id = :parentId 
        ORDER BY rr.createdAt ASC
    """)
    List<ReviewReply> findChildRepliesByParentIdAdmin(@Param("parentId") Long parentId);

    /**
     * Finds all child review replies for a given parent reply ID.
     *
     * @param parentId the ID of the parent review reply
     * @return a list of child ReviewReply entities
     */
    @Query("SELECT rr FROM ReviewReply rr " +
            "WHERE rr.parentReply.id = :parentId " +
            "ORDER BY rr.createdAt ASC")
    List<ReviewReply> findAllChildRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Finds all top-level review replies for a given review ID.
     *
     * @param reviewId the ID of the review
     * @return a list of top-level ReviewReply entities
     */
    @Query("SELECT rr FROM ReviewReply rr " +
            "WHERE rr.review.id = :reviewId AND rr.parentReply IS NULL " +
            "ORDER BY rr.createdAt ASC")
    List<ReviewReply> findAllTopLevelRepliesByReviewId(@Param("reviewId") Long reviewId);
}
