/*
 * @ {#} CalculateShippingRequest.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: DTO for calculating shipping cost
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public record CalculateShippingRequest(
        Integer serviceId,

        Integer serviceTypeId,

        String toWardCode,

        Integer toDistrictId,

        Integer fromDistrictId,

        Integer weight,

        Integer length,

        Integer width,

        Integer height
) {}
