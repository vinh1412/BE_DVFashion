/*
 * @ {#} GhnProvincesApiResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import java.util.List;

/*
 * @description: DTO for GHN provinces API response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record GhnProvincesApiResponse(
        Integer code,

        String message,

        List<GhnProvinceResponse> data
) {}
