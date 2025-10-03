/*
 * @ {#} AddressResponse.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

/*
 * @description: Service interface for handling address-related operations.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.dtos.request.CreateAddressRequest;
import vn.edu.iuh.fit.dtos.request.UpdateAddressRequest;
import vn.edu.iuh.fit.dtos.response.AddressResponse;

public interface AddressService {
    /**
     * Creates a new address based on the provided request data.
     *
     * @param request the request data for creating a new address
     * @return the created address response
     */
    AddressResponse createAddress(CreateAddressRequest request);

    /**
     * Updates an existing address identified by the given ID with the provided request data.
     *
     * @param id      the ID of the address to be updated
     * @param request the request data for updating the address
     * @return the updated address response
     */
    AddressResponse updateAddress(Long id, UpdateAddressRequest request);
}
