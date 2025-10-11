/*
 * @ {#} UpdateAddressRequest.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: DTO for updating an existing address.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
public record UpdateAddressRequest(
        String fullName,

        String phone,

        String country,

        String city,

        String district,

        String ward,

        String street,

        Boolean isDefault
) {
}
