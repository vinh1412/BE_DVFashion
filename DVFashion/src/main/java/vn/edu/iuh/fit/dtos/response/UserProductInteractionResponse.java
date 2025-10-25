/*
 * @ {#} UserProductInteractionResponse.java   1.0     25/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for user-product interaction response.
 * @author: Tran Hien Vinh
 * @date:   25/10/2025
 * @version:    1.0
 */
public record UserProductInteractionResponse(
    Long id,

    Long userId,

    Long productId,

    String interactionType,

    String rating,

    Integer interactionCount,

    String createdAt
) {}
