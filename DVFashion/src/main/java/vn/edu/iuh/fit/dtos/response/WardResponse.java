/*
 * @ {#} WardResponse.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for ward response
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record WardResponse(
        String wardCode,

        Integer districtId,

        String wardName
) {}
