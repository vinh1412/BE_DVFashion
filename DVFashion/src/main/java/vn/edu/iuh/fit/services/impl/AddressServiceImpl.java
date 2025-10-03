/*
 * @ {#} AddressServiceImpl.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.request.CreateAddressRequest;
import vn.edu.iuh.fit.dtos.response.AddressResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Address;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.DuplicateAddressException;
import vn.edu.iuh.fit.mappers.AddressMapper;
import vn.edu.iuh.fit.repositories.AddressRepository;
import vn.edu.iuh.fit.services.AddressService;
import vn.edu.iuh.fit.services.UserService;

/*
 * @description: Service implementation for handling address-related operations.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    private final UserService userService;

    private final AddressMapper addressMapper;

    @Transactional
    @Override
    public AddressResponse createAddress(CreateAddressRequest request) {
        // Get current authenticated user
        UserResponse user = userService.getCurrentUser();

        // Fetch the full user entity from the database
        User userEntity = userService.findById(user.getId());

        // Duplicate check (normalize by trimming)
        if (addressRepository.existsDuplicate(
                userEntity.getId(),
                request.phone().trim(),
                request.country().trim(),
                request.city().trim(),
                request.district().trim(),
                request.ward().trim(),
                request.street().trim()
        )) {
            throw new DuplicateAddressException("Address with this phone already exists.");
        }

        // If the new address is marked as default, clear existing default addresses for the user
        if (Boolean.TRUE.equals(request.isDefault())) {
            addressRepository.clearDefaultForUser(user.getId());
        }

        // Map the request DTO to an Address entity
        Address address = addressMapper.toEntity(request, userEntity);

        // Save the new address to the database
        Address saved = addressRepository.save(address);

        return addressMapper.toResponse(saved);
    }
}
