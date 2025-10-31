/*
 * @ {#} GhnWardResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * @description: DTO for GHN ward response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record GhnWardResponse(
        @JsonProperty("WardCode")
        String wardCode,

        @JsonProperty("DistrictID")
        Integer districtId,

        @JsonProperty("WardName")
        String wardName
) {}
