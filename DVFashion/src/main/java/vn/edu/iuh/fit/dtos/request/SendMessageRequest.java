/*
 * @ {#} SendMessageRequest.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * @description: Request DTO for sending a chat message
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
public record SendMessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
        String content
) {}
