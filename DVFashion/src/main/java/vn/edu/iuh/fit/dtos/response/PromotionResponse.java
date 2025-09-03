/*
 * @ {#} PromotionResponse.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description: Response DTO for promotion details
 * @author: Tran Hien Vinh
 * @date:   03/09/2025
 * @version:    1.0
 */
public record PromotionResponse(
        Long id,

        String name,

        String description,

        PromotionType type,

        BigDecimal value,

        BigDecimal minOrderAmount,

        Integer maxUsages,

        Integer currentUsages,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Boolean active
) {}