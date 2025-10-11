/*
 * @ {#} AddressServiceImpl.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.request.CreateAddressRequest;
import vn.edu.iuh.fit.dtos.request.UpdateAddressRequest;
import vn.edu.iuh.fit.dtos.response.AddressResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Address;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.entities.embedded.ShippingInfo;
import vn.edu.iuh.fit.exceptions.DuplicateAddressException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.AddressMapper;
import vn.edu.iuh.fit.repositories.AddressRepository;
import vn.edu.iuh.fit.services.AddressService;
import vn.edu.iuh.fit.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

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

        String phone = request.phone().trim();
        String country = request.country().trim();
        String city = request.city().trim();
        String district = request.district().trim();
        String ward = request.ward().trim();
        String street = request.street().trim();

        // Duplicate check (normalize by trimming)
        if (addressRepository.existsDuplicate(userEntity.getId(), phone, country, city, district, ward, street)) {
            throw new DuplicateAddressException("Address with this phone already exists.");
        }

        // Soft-deleted duplicate -> restore instead of creating new
        Address restored = addressRepository.findSoftDeletedDuplicate(
                userEntity.getId(), phone, country, city, district, ward, street
        ).map(a -> {
            ShippingInfo info = a.getShippingInfo();
            info.setFullName(request.fullName());
            info.setPhone(phone);
            info.setCountry(country);
            info.setCity(city);
            info.setDistrict(district);
            info.setWard(ward);
            info.setStreet(street);
            a.setDeleted(false);
            a.setDeletedAt(null);

            if (Boolean.TRUE.equals(request.isDefault())) {
                addressRepository.clearDefaultForUser(userEntity.getId());
                a.setDefault(true);
            }
            return addressRepository.save(a);
        }).orElse(null);

        if (restored != null) {
            return addressMapper.toResponse(restored);
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

    @Transactional
    @Override
    public AddressResponse updateAddress(Long id, UpdateAddressRequest request) {
        UserResponse current = userService.getCurrentUser();
        User userEntity = userService.findById(current.getId());

        // Check if the address exists and belongs to the current user
        Address address = addressRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + id));

        if (address.isDeleted()) {
            throw new NotFoundException("Address has been deleted.");
        }

        // Check for duplicates
        if (addressRepository.existsDuplicate(
                userEntity.getId(),
                request.phone(),
                request.country(),
                request.city(),
                request.district(),
                request.ward(),
                request.street()
        )) {
            throw new DuplicateAddressException("Another address with this phone and location exists.");
        }

        // Update fields if they are provided in the request
        ShippingInfo info = address.getShippingInfo();

        if (request.fullName() != null && !request.fullName().trim().isEmpty()) {
            info.setFullName(request.fullName().trim());
        }
        if (request.phone() != null && !request.phone().trim().isEmpty()) {
            info.setPhone(request.phone().trim());
        }
        if (request.country() != null && !request.country().trim().isEmpty()) {
            info.setCountry(request.country().trim());
        }
        if (request.city() != null && !request.city().trim().isEmpty()) {
            info.setCity(request.city().trim());
        }
        if (request.district() != null && !request.district().trim().isEmpty()) {
            info.setDistrict(request.district().trim());
        }
        if (request.ward() != null && !request.ward().trim().isEmpty()) {
            info.setWard(request.ward().trim());
        }
        if (request.street() != null && !request.street().trim().isEmpty()) {
            info.setStreet(request.street().trim());
        }

        // Handle default address logic
        if (request.isDefault() != null && request.isDefault()) {
            addressRepository.clearDefaultForUser(userEntity.getId());
            address.setDefault(true);
        } else if (request.isDefault() != null && !request.isDefault()) {
            address.setDefault(false);
        }

        Address updated = addressRepository.save(address);
        return addressMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long id) {
        UserResponse current = userService.getCurrentUser();

        Address address = addressRepository.findByIdAndUserId(id, current.getId())
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + id));

        if (address.isDeleted()) {
            throw new NotFoundException("Address not found with id: " + id);
        }

        return addressMapper.toResponse(address);
    }

    @Override
    public List<AddressResponse> getAddresses() {
        UserResponse current = userService.getCurrentUser();

        List<Address> addresses = addressRepository.findAllByUserIdAndIsDeletedFalse(
                current.getId(),
                Sort.by(Sort.Direction.DESC, "createAt")
        );

        return addresses.stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    public void softDeleteAddress(Long id) {
        UserResponse current = userService.getCurrentUser();

        Address address = addressRepository.findByIdAndUserId(id, current.getId())
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + id));

        if (address.isDeleted()) return;

//        boolean wasDefault = address.isDefault();
        address.setDeleted(true);
        address.setDeletedAt(LocalDateTime.now());
        address.setDefault(false);

        addressRepository.save(address);

//        // Optionally promote another address if default removed
//        if (wasDefault) {
//            addressRepository.findAllByUserIdAndIsDeletedFalse(current.getId(),
//                            Sort.by(Sort.Direction.DESC, "createAt"))
//                    .stream()
//                    .findFirst()
//                    .ifPresent(a -> {
//                        a.setDefault(true);
//                        addressRepository.save(a);
//                    });
//        }
    }
}
