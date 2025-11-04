/*
 * @ {#} GhnCalculateShippingRequest.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import lombok.Builder;

/*
 * @description: DTO for calculating shipping cost via GHN service
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Builder
public record GhnCalculateShippingRequest(
        Integer service_id,

        Integer service_type_id,

        String to_ward_code,

        Integer to_district_id,

        Integer from_district_id,

        Integer weight,

        Integer length,

        Integer width,

        Integer height
) {}
