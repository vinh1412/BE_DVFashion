/*
 * @ {#} GhnLeadTimeRequest.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: DTO for calculating lead time between two locations
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public record GhnLeadTimeRequest(
        Integer from_district_id,

        String from_ward_code,

        Integer to_district_id,

        String to_ward_code,

        Integer service_id
) {}
