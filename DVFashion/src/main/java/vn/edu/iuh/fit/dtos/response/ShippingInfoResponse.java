/*
 * @ {#} ShippingInfoResponse.java   1.0     22/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: Response DTO for shipping information
 * @author: Tran Hien Vinh
 * @date:   22/09/2025
 * @version:    1.0
 */
public record ShippingInfoResponse (
         String fullName,

         String phone,

         String email,

         String fullAddress
){}
