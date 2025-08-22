/*
 * @ {#} LoginRequest.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * @description: Request DTO for user sign-in
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */

public record SignInRequest (
    @NotBlank(message = "Email or phone is required")
     String username,

    @NotBlank(message = "Password is required")
    String password
){}
