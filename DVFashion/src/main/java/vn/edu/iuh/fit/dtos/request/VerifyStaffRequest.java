/*
 * @ {#} VerifyStaffRequest.java   1.0     02/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * @description: Request DTO for verifying staff accounts
 * @author: Tran Hien Vinh
 * @date:   02/09/2025
 * @version:    1.0
 */
public record VerifyStaffRequest (
        @NotBlank(message = "Verification code is required")
        @Size(min = 6, max = 6, message = "Verification code must be 6 characters")
        String verificationCode,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "New password must contain at least one letter and one number, and be at least 8 characters"
        )
        String newPassword
){}
