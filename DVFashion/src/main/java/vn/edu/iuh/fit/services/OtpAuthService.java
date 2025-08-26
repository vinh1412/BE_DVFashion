/*
 * @ {#} OtpAuthService.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ResetPasswordOtpRequest;
import vn.edu.iuh.fit.dtos.request.VerifyOtpRequest;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public interface OtpAuthService {
    String verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordOtpRequest request);
}
