/*
 * @ {#} ReviewResponse.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;


import vn.edu.iuh.fit.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: DTO class representing the response for a product review.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public record ReviewResponse(
     Long id,

     Long orderId,

     String orderNumber,

     Long productVariantId,

     String productName,

     String variantName,

     Integer rating,

     String comment,

     ReviewStatus status,

     Integer helpfulCount,

     LocalDateTime createdAt,

     LocalDateTime updatedAt,

     List<String> imageUrls,

     String adminComment,

     UserSummaryResponse user
){}
