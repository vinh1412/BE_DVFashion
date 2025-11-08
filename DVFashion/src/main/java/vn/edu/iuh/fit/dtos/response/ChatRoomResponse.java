/*
 * @ {#} ChatRoomResponse.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.ChatRoomStatus;
import vn.edu.iuh.fit.enums.ChatRoomType;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for chat rooms
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Builder
public record ChatRoomResponse(
        Long id,

        String roomCode,

        ChatRoomType type,

        String customerName,

        String guestName,

        String guestEmail,

        String guestPhone,

        ChatRoomStatus status,

        LocalDateTime lastMessageAt,

        Integer unreadCustomerCount,

        Integer unreadAdminCount,

        LocalDateTime createdAt,

        ChatMessageResponse lastMessage
) {}
