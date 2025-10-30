/*
 * @ {#} GhnLeadTimeResponse.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for GHN lead time API response
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public record GhnLeadTimeResponse(
        Integer code,

        String message,

        GhnLeadTimeData data
) {
    public record GhnLeadTimeData(
            String leadtime,

            String order_date
    ) {}
}
