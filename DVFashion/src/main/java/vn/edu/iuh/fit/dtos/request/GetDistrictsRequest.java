/*
 * @ {#} GetDistrictsRequest.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: DTO for retrieving districts by province ID
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
public record GetDistrictsRequest(
        Integer province_id
) {}
