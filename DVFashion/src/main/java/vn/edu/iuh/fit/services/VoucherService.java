/*
 * @ {#} VoucherService.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.CreateVoucherRequest;
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
}
