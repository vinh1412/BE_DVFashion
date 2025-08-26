/*
 * @ {#} ResetPasswordOtpRequest.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public record ResetPasswordOtpRequest(
        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "Password must contain at least one letter and one number, and be at least 8 characters"
        )
        String newPassword
) {}
