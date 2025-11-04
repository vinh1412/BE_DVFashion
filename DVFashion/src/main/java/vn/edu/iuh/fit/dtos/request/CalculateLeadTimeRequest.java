/*
 * @ {#} CalculateLeadTimeRequest.java   1.0     29/10/2025
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
public record CalculateLeadTimeRequest(
        Integer fromDistrictId,

        String fromWardCode,

        Integer toDistrictId,

        String toWardCode,

        Integer serviceId
) {}
