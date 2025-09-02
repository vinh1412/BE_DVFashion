/*
 * @ {#} EmailService.java   1.0     26/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.ForgotPasswordRequest;
import vn.edu.iuh.fit.dtos.request.ResetPasswordMailRequest;
import vn.edu.iuh.fit.entities.User;

/*
 * @description: Service interface for handling email-related operations
 * @author: Tran Hien Vinh
 * @date:   26/08/2025
 * @version:    1.0
 */
public interface EmailService {
    /**
     * Sends a password reset email to the user based on the provided request.
     *
     * @param request the ForgotPasswordRequest containing the user's email
     */
    void sendPasswordResetEmail(ForgotPasswordRequest request);

    /**
     * Validates the provided password reset token and returns the associated user.
     *
     * @param token the password reset token to validate
     * @return the User associated with the valid token
     */
    User validatePasswordResetToken(String token);

    /**
     * Resets the user's password based on the provided request.
     *
     * @param request the ResetPasswordRequest containing the new password and token
     */
    void resetPassword(ResetPasswordMailRequest request);

    /**
     * Sends a verification code email to the specified email address.
     *
     * @param email            the recipient's email address
     * @param fullName         the recipient's full name
     * @param verificationCode the verification code to be sent
     */
    void sendVerificationCode(String email, String fullName, String password, String verificationCode);
}
