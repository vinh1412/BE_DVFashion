/*
 * @ {#} PromotionResponse.java   1.0     03/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import vn.edu.iuh.fit.enums.PromotionType;

import java.time.LocalDateTime;
import java.util.List;

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

        LocalDateTime startDate,

        LocalDateTime endDate,

        String bannerUrl,

        Boolean active,

        List<PromotionProductResponse> promotionProducts
) {}