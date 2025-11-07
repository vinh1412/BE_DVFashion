/*
 * @ {#} ChatMapper.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

/*
 * @description: Mapper class for converting chat-related entities to DTO responses
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.ChatMessageAttachmentResponse;
import vn.edu.iuh.fit.dtos.response.ChatMessageResponse;
import vn.edu.iuh.fit.dtos.response.ChatRoomResponse;
import vn.edu.iuh.fit.entities.ChatMessage;
import vn.edu.iuh.fit.entities.ChatMessageAttachment;
import vn.edu.iuh.fit.entities.ChatRoom;
import vn.edu.iuh.fit.enums.Language;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    public ChatRoomResponse mapToResponse(ChatRoom chatRoom, Language language) {
        String customerName = null;
        if (chatRoom.getCustomer() != null) {
            customerName = chatRoom.getCustomer().getFullName();
        }

        ChatMessageResponse lastMessage = null;
        if (!chatRoom.getMessages().isEmpty()) {
            ChatMessage lastMsg = chatRoom.getMessages().get(chatRoom.getMessages().size() - 1);
            lastMessage = mapToMessageResponse(lastMsg, language);
        }

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .roomCode(chatRoom.getRoomCode())
                .type(chatRoom.getType())
                .customerName(customerName)
                .guestName(chatRoom.getGuestName())
                .guestEmail(chatRoom.getGuestEmail())
                .guestPhone(chatRoom.getGuestPhone())
                .status(chatRoom.getStatus())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCustomerCount(chatRoom.getUnreadCustomerCount())
                .unreadAdminCount(chatRoom.getUnreadAdminCount())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessage(lastMessage)
                .build();
    }

    public ChatMessageResponse mapToMessageResponse(ChatMessage message, Language language) {
        String senderName = getSenderName(message);

        List<ChatMessageAttachmentResponse> attachments = message.getAttachments().stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());

        return ChatMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .senderType(message.getSenderType())
                .senderName(senderName)
                .status(message.getStatus())
                .isReadByCustomer(message.getIsReadByCustomer())
                .isReadByAdmin(message.getIsReadByAdmin())
                .createdAt(message.getCreatedAt())
                .attachments(attachments)
                .build();
    }

    public ChatMessageAttachmentResponse mapToAttachmentResponse(ChatMessageAttachment attachment) {
        return ChatMessageAttachmentResponse.builder()
                .id(attachment.getId())
                .type(attachment.getType())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private String getSenderName(ChatMessage message) {
        switch (message.getSenderType()) {
            case ADMIN:
                return "Shop Assistant";
            case CUSTOMER:
                if (message.getSender() != null) {
                    return message.getSender().getFullName();
                }
                return "Customer";
            case GUEST:
                return message.getChatRoom().getGuestName() != null ?
                        message.getChatRoom().getGuestName() : "Guest";
            default:
                return "Unknown";
        }
    }
}
