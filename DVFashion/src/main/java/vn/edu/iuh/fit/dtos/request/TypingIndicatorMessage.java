/*
 * @ {#} TypingIndicatorMessage.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import lombok.Builder;

/*
 * @description: DTO for typing indicator messages in chat
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Builder
public record TypingIndicatorMessage(
        String senderName,

        String senderType,

        boolean isTyping
) {}
