/*
 * @ {#} UserInteractionRequest.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.dtos.request;

import lombok.Builder;

import java.math.BigDecimal;

/*
 * @description: DTO for user interaction request
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Builder
public record UserInteractionRequest(
         Long userId,
         Long productId,
         String interactionType,
         BigDecimal rating
) {}
