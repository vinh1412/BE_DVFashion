/*
 * @ {#} UserSummaryResponse.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO class for summarizing user information in responses.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
public record UserSummaryResponse(
        Long id,

        String fullName
) {}
