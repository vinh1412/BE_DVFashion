/*
 * @ {#} GhnDistrictResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * @description: DTO for GHN district response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record GhnDistrictResponse(
        @JsonProperty("DistrictID")
        Integer districtId,

        @JsonProperty("ProvinceID")
        Integer provinceId,

        @JsonProperty("DistrictName")
        String districtName,

        @JsonProperty("Code")
        String code
) {}
