/*
 * @ {#} ShippingInfoMapper.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.request.ShippingInfoRequest;
import vn.edu.iuh.fit.dtos.response.ShippingInfoResponse;
import vn.edu.iuh.fit.entities.embedded.ShippingInfo;

/*
 * @description: Mapper class for converting ShippingInfo entities to ShippingInfoResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@Component
public class ShippingInfoMapper {
    public ShippingInfo mapToShippingInfo(ShippingInfoRequest request) {
        return ShippingInfo.builder()
                .fullName(request.fullName())
                .phone(request.phone())
                .country(request.country())
                .city(request.city())
                .district(request.district())
                .ward(request.ward())
                .street(request.street())
                .build();
    }

    public ShippingInfoResponse mapShippingInfoResponse(ShippingInfo shippingInfo, String userEmail) {
        String fullAddress = String.join(", ",
                shippingInfo.getStreet(),
                shippingInfo.getWard(),
                shippingInfo.getDistrict(),
                shippingInfo.getCity(),
                shippingInfo.getCountry()
        );
        return new ShippingInfoResponse(
                shippingInfo.getFullName(),
                shippingInfo.getPhone(),
                userEmail,
                fullAddress
        );
    }
}
