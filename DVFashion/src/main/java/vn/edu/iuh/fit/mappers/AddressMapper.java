/*
 * @ {#} AddressMapper.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.request.CreateAddressRequest;
import vn.edu.iuh.fit.dtos.response.AddressResponse;
import vn.edu.iuh.fit.entities.Address;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.entities.embedded.ShippingInfo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @description: Mapper class for converting between Address entities and DTOs.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Component
public class AddressMapper {
    // Convert CreateAddressRequest DTO to Address entity
    public Address toEntity(CreateAddressRequest req, User user) {
        ShippingInfo info = ShippingInfo.builder()
                .fullName(req.fullName())
                .phone(req.phone())
                .country(req.country())
                .city(req.city())
                .district(req.district())
                .ward(req.ward())
                .street(req.street())
                .build();

        return Address.builder()
                .shippingInfo(info)
                .isDefault(Boolean.TRUE.equals(req.isDefault()))
                .user(user)
                .build();
    }

    // Convert Address entity to AddressResponse DTO
    public AddressResponse toResponse(Address address) {
        ShippingInfo s = address.getShippingInfo();
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(s.getFullName())
                .phone(s.getPhone())
                .country(s.getCountry())
                .city(s.getCity())
                .district(s.getDistrict())
                .ward(s.getWard())
                .street(s.getStreet())
                .isDefault(address.isDefault())
                .fullAddress(buildFullAddress(s))
                .build();
    }

    // Helper method to build a full address string from ShippingInfo
    private String buildFullAddress(ShippingInfo s) {
        return Stream.of(s.getStreet(), s.getWard(), s.getDistrict(), s.getCity(), s.getCountry())
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(", "));
    }
}
