/*
 * @ {#} CreateStaffRequest.java   1.0     02/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * @description: Request DTO for creating a new staff member
 * @author: Tran Hien Vinh
 * @date:   02/09/2025
 * @version:    1.0
 */
public record CreateStaffRequest (
    @NotBlank(message = "Full name is required")
    @Size(min = 6, message = "Full name must be at least 6 characters")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Phone number is required")
    String phone
){}
