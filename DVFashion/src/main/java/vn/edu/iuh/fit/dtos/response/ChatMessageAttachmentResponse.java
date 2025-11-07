/*
 * @ {#} ChatMessageAttachmentResponse.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Builder;
import vn.edu.iuh.fit.enums.AttachmentType;

import java.time.LocalDateTime;

/*
 * @description: Response DTO for chat message attachments
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Builder
public record ChatMessageAttachmentResponse(
        Long id,

        AttachmentType type,

        String fileName,

        String fileUrl,

        Long fileSize,

        String mimeType,

        LocalDateTime createdAt
) {}
