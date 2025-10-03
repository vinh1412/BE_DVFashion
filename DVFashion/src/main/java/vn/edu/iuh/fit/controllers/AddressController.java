/*
 * @ {#} AddressController.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.CreateAddressRequest;
import vn.edu.iuh.fit.dtos.request.UpdateAddressRequest;
import vn.edu.iuh.fit.dtos.response.AddressResponse;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.services.AddressService;

/*
 * @description: Controller class for managing address-related HTTP requests.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/addresses")
public class AddressController {
     private final AddressService addressService;

     @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Address created successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }
}
