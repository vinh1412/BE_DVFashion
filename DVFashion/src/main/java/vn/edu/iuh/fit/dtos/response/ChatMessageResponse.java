/*
 * @ {#} ChatMessageResponse.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.MessageSender;
import vn.edu.iuh.fit.enums.MessageStatus;
import vn.edu.iuh.fit.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Response DTO for chat messages
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Builder
public record ChatMessageResponse(
        Long id,

        String content,

        MessageType messageType,

        MessageSender senderType,

        String senderName,

        MessageStatus status,

        Boolean isReadByCustomer,

        Boolean isReadByAdmin,

        LocalDateTime createdAt,

        List<ChatMessageAttachmentResponse> attachments
) {}
