/*
 * @ {#} ReviewReply.java   1.0     03/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.ReviewReplyStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Entity class representing a reply to a product review.
 * @author: Tran Hien Vinh
 * @date:   03/11/2025
 * @version:    1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review_replies")
public class ReviewReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private ReviewReply parentReply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewReplyStatus status;

    @Column(name = "edited", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean edited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewReply> childReplies = new ArrayList<>();

    @OneToMany(mappedBy = "reviewReply", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewReplyTranslation> translations = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReviewReplyStatus.APPROVED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
