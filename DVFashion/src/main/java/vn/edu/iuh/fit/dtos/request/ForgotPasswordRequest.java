/*
 * @ {#} ForgotPasswordRequest.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
 * @description: Request DTO for forgot password
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
public record ForgotPasswordRequest (
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
){
}
