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
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.services.AddressService;
import vn.edu.iuh.fit.services.GhnService;

import java.util.List;

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

    private final GhnService ghnService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Address created successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(addressService.updateAddress(id, request), "Address updated successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAddressById(id), "Address retrieved successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddressesByUser() {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAddresses(), "Addresses retrieved successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id) {
        addressService.softDeleteAddress(id);
        return ResponseEntity.ok(ApiResponse.noContent("Address deleted successfully."));
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces() {
        List<ProvinceResponse> provinces = ghnService.getProvinces();
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/provinces/{provinceId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByProvinceId(@PathVariable("provinceId") Integer provinceId) {
        List<DistrictResponse> districts = ghnService.getDistricts(provinceId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/provinces/{districtId}/wards")
    public ResponseEntity<List<WardResponse>> getWardsByDistrictId(@PathVariable("districtId") Integer districtId) {
        List<WardResponse> wards = ghnService.getWards(districtId);
        return ResponseEntity.ok(wards);
    }
}
