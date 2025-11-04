/*
 * @ {#} AddressResponse.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;

/*
 * @description: DTO for address response.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Builder
public record AddressResponse(
        Long id,

        String fullName,

        String phone,

        String country,

        String city,

        String district,

        String ward,

        String street,

        boolean isDefault,

        String fullAddress
) {}
