/*
 * @ {#} GhnProvinceResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/*
 * @description: DTO for GHN province response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record GhnProvinceResponse(
        @JsonProperty("ProvinceID")
        Integer provinceId,

        @JsonProperty("NameExtension")
        List<String> nameExtension
) {}
