/*
 * @ {#} VoucherService.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
import vn.edu.iuh.fit.dtos.request.UpdateVoucherRequest;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.enums.Language;

/*
 * @description: Service interface for Voucher operations
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
public interface VoucherService {
    /**
     * Create a new voucher based on the provided request and language.
     *
     * @param request  the request containing voucher details
     * @param language the language for the voucher translation
     * @return the created VoucherResponse
     */
    VoucherResponse createVoucher(CreateVoucherRequest request, Language language);

    /**
     * Update an existing voucher identified by its ID with the provided request and language.
     *
     * @param id       the ID of the voucher to update
     * @param request  the request containing updated voucher details
     * @param language the language for the voucher translation
     * @return the updated VoucherResponse
     */
    VoucherResponse updateVoucher(Long id, UpdateVoucherRequest request, Language language);

    /**
     * Remove a product from a product-specific voucher.
     *
     * @param voucherId the ID of the voucher
     * @param productId the ID of the product to remove
     * @param language  the language for the response
     * @return the updated VoucherResponse
     */
    VoucherResponse removeProductFromVoucher(Long voucherId, Long productId, Language language);
}
