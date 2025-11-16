/*
 * @ {#} ChatService.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CreateChatRoomRequest;
import vn.edu.iuh.fit.dtos.request.SendMessageRequest;
import vn.edu.iuh.fit.dtos.response.ChatMessageResponse;
import vn.edu.iuh.fit.dtos.response.ChatRoomResponse;
import vn.edu.iuh.fit.enums.Language;

import java.util.List;

/*
 * @description: Service interface for chat functionalities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
public interface ChatService {
    /**
     * Create a chat room for a guest user.
     *
     * @param request  the request containing chat room details
     * @param language the language for localization
     * @return the created chat room response
     */
    ChatRoomResponse createGuestChatRoom(CreateChatRoomRequest request, Language language);

    /**
     * Create a chat room for a logged-in customer.
     *
     * @param language the language for localization
     * @return the created chat room response
     */
    ChatRoomResponse createCustomerChatRoom(Language language);

    /**
     * Retrieve a chat room by its unique code.
     *
     * @param roomCode the unique code of the chat room
     * @param language the language for localization
     * @return the chat room response
     */
    ChatRoomResponse getChatRoomByCode(String roomCode, Language language);

    /**
     * Retrieve chat messages for a specific chat room with pagination.
     *
     * @param roomCode the unique code of the chat room
     * @param page     the page number for pagination
     * @param size     the number of messages per page
     * @param language the language for localization
     * @return the list of chat message responses
     */
    List<ChatMessageResponse> getChatMessages(String roomCode, int page, int size, Language language);

    /**
     * Send a message in a specific chat room.
     *
     * @param roomCode the unique code of the chat room
     * @param request  the request containing message details
     * @param language the language for localization
     * @return the sent chat message response
     */
    ChatMessageResponse sendMessage(String roomCode, SendMessageRequest request, Language language);

    /**
     * Send a message with an attachment in a specific chat room.
     *
     * @param roomCode the unique code of the chat room
     * @param content  the content of the message
     * @param file     the attachment file
     * @param language the language for localization
     * @return the sent chat message response
     */
    ChatMessageResponse sendMessageWithAttachment(String roomCode, String content, MultipartFile file, Language language);

    /**
     * Mark all messages in a chat room as read.
     *
     * @param roomCode the unique code of the chat room
     */
    void markMessagesAsRead(String roomCode);

    /**
     * Retrieve chat rooms for admin with pagination.
     *
     * @param page     the page number for pagination
     * @param size     the number of chat rooms per page
     * @param language the language for localization
     * @return the list of chat room responses
     */
    List<ChatRoomResponse> getAdminChatRooms(int page, int size, Language language);

    String getChatRoomCodeByUserId(Long userId);
}
