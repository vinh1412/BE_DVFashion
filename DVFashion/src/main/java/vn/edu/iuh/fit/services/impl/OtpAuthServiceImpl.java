/*
 * @ {#} OtpAuthServiceImpl.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.ResetPasswordOtpRequest;
import vn.edu.iuh.fit.dtos.request.VerifyOtpRequest;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.FirebaseAuthCustomsException;
import vn.edu.iuh.fit.services.OtpAuthService;
import vn.edu.iuh.fit.services.UserService;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class OtpAuthServiceImpl implements OtpAuthService {
    private final UserService userService;

    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        try {
            // Get the ID token from the request
            String idToken = request.idToken();

            // Verify the ID token using Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // Extract the phone number from the decoded token
            String phoneNumber = decodedToken.getClaims().get("phone_number").toString();

            // Check if the phone number exists in your user database
            if (!userService.existsByPhone(phoneNumber)) {
                throw new AlreadyExistsException("Phone number does not exist");
            }

            // Return the phone number if verification is successful
            return phoneNumber;
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthCustomsException("Error verifying ID token: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(ResetPasswordOtpRequest request) {
        // Extract phone number and new password from the request
        String phone = request.phone();
        String newPassword = request.newPassword();

            // Check if the phone number exists in your user database
            if (!userService.existsByPhone(phone)) {
                throw new AlreadyExistsException("Phone number does not exist");
            }

            // Update the user's password
            userService.updatePassword(phone, newPassword);
    }
}
