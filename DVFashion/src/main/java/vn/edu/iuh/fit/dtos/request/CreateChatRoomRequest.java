/*
 * @ {#} CreateChatRoomRequest.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/*
 * @description: Request DTO for creating a chat room
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Builder
public record CreateChatRoomRequest(
        @NotBlank(message = "Guest name is required")
        String guestName,

        String guestPhone
) {}
