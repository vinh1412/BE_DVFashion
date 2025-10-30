/*
 * @ {#} GhnCalculateShippingResponse.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for GHN shipping cost calculation response
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public record GhnCalculateShippingResponse(
        Integer code,

        String message,

        GhnShippingData data
) {
    public record GhnShippingData(
            Integer total,

            Integer service_fee,

            Integer insurance_fee,

            Integer pick_station_fee,

            Integer coupon_value,

            Integer r2s_fee,

            Integer return_again,

            Integer document_return,

            Integer double_check,

            Integer cod_fee,

            Integer pick_remote_areas_fee,

            Integer deliver_remote_areas_fee,

            Integer cod_failed_fee
    ) {}
}
