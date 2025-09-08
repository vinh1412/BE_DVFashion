/*
 * @ {#} OtpAuthService.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ResetPasswordOtpRequest;
import vn.edu.iuh.fit.dtos.request.VerifyOtpRequest;

/*
 * @description: Service interface for OTP-based authentication
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public interface OtpAuthService {
    /**
     * Verify OTP for forgot password
     * @param request the verify OTP request containing the ID token
     * @return the phone number associated with the OTP
     */
    String verifyOtpForgotPassword(VerifyOtpRequest request);

    /**
     * Reset password using OTP
     * @param request the reset password request containing phone number and new password
     */
    void resetPassword(ResetPasswordOtpRequest request);

    /**
     * Verify OTP for sign-up
     * @param request the verify OTP request containing the ID token
     * @return the phone number associated with the OTP
     */
    String verifyOtpForSignUp(VerifyOtpRequest request);
}
