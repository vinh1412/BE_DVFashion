/*
 * @ {#} ResetPasswordRequest.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * @description: Request DTO for reset password
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        String token,

        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "Password must contain at least one letter and one number, and be at least 8 characters"
        )
        String newPassword
) {}
