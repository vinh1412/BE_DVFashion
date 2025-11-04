/*
 * @ {#} DistrictResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for district response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record DistrictResponse(
        Integer districtId,

        Integer provinceId,

        String districtName,

        String code
) {}
