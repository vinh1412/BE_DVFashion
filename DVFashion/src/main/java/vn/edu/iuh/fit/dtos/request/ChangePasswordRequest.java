/*
 * @ {#} ChangePasswordRequest.java   1.0     04/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * @description: DTO for change password request
 * @author: Tran Hien Vinh
 * @date:   04/09/2025
 * @version:    1.0
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "New password must contain at least one letter and one number, and be at least 8 characters"
        )
        String newPassword
) {
}
