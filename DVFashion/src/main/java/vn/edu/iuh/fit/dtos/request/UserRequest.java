/*
 * @ {#} UserRequest.java   1.0     01/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

/*
 * @description: DTO for user request data
 * @author: Tran Hien Vinh
 * @date:   01/09/2025
 * @version:    1.0
 */
public record UserRequest(
    @Size(min = 6, message = "Full name must be at least 6 characters")
    String fullName,

    @Email(message = "Email should be valid")
    String email,

    String phone,

    String gender,

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    String dob
) {}
