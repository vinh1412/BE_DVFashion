/*
 * @ {#} ChatController.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import com.cloudinary.Api;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.CreateChatRoomRequest;
import vn.edu.iuh.fit.dtos.request.SendMessageRequest;
import vn.edu.iuh.fit.dtos.request.TypingIndicatorMessage;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.ChatMessageResponse;
import vn.edu.iuh.fit.dtos.response.ChatRoomResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.services.AIChatService;
import vn.edu.iuh.fit.services.ChatService;

import java.util.List;
import java.util.Map;

/*
 * @description: Controller for managing chat functionalities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("${web.base-path}/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    private final AIChatService aiChatService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(
            @Valid @RequestBody CreateChatRoomRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {
        // Validate guest name
        if (request.guestName() == null || request.guestName().trim().isEmpty()) {
            throw new BadRequestException("Guest name is required");
        }

        ChatRoomResponse response = chatService.createGuestChatRoom(request, language);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Chat room created successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @PostMapping("/rooms/customer")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createCustomerChatRoom(
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        ChatRoomResponse response = chatService.createCustomerChatRoom(language);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Customer chat room created successfully"));
    }

    @GetMapping("/rooms/{roomCode}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @PathVariable String roomCode,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        ChatRoomResponse response = chatService.getChatRoomByCode(roomCode, language);

        return ResponseEntity.ok(ApiResponse.success(response, "Chat room retrieved successfully"));
    }

    @GetMapping("/rooms/{roomCode}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable String roomCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        List<ChatMessageResponse> messages = chatService.getChatMessages(roomCode, page, size, language);

        return ResponseEntity.ok(ApiResponse.success(messages, "Chat messages retrieved successfully"));
    }

    @PostMapping("/rooms/{roomCode}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable String roomCode,
            @Valid @RequestBody SendMessageRequest request,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        ChatMessageResponse response = chatService.sendMessage(roomCode, request, language);

        // Send real-time message
        messagingTemplate.convertAndSend("/topic/chat/" + roomCode, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Message sent successfully"));
    }

    @PostMapping("/rooms/{roomCode}/messages/upload")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessageWithAttachment(
            @PathVariable String roomCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "lang", defaultValue = "VI") Language language) {

        ChatMessageResponse response = chatService.sendMessageWithAttachment(roomCode, content, file, language);

        // Send real-time message
        messagingTemplate.convertAndSend("/topic/chat/" + roomCode, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Message with attachment sent successfully"));
    }

    @PostMapping("/rooms/{roomCode}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(@PathVariable String roomCode) {
        chatService.markMessagesAsRead(roomCode);

        return ResponseEntity.ok(ApiResponse.noContent("Messages marked as read successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/admin/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getAdminChatRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "lang", defaultValue = "VI") Language language
            ) {

        List<ChatRoomResponse> rooms = chatService.getAdminChatRooms(page, size, language);

        return ResponseEntity.ok(ApiResponse.success(rooms, "Admin chat rooms retrieved successfully"));
    }

    // WebSocket message handlers
    @MessageMapping("/chat/{roomCode}")
    public void handleChatMessage(
            @DestinationVariable String roomCode,
            @Payload SendMessageRequest message) {

        try {
            ChatMessageResponse response = chatService.sendMessage(roomCode, message, Language.VI);

            // Broadcast to all subscribers of this room
            messagingTemplate.convertAndSend("/topic/chat/" + roomCode, response);

        } catch (Exception e) {
            log.error("Error handling chat message: ", e);
        }
    }

    @MessageMapping("/chat/{roomCode}/typing")
    public void handleTypingIndicator(
            @DestinationVariable String roomCode,
            @Payload TypingIndicatorMessage message) {

        // Broadcast typing indicator
        messagingTemplate.convertAndSend("/topic/chat/" + roomCode + "/typing", message);
    }

    @PostMapping("/ai")
    public ResponseEntity<ApiResponse<JsonNode>> chatAI(@RequestBody Map<String, String> request) {
        JsonNode response = aiChatService.sendToAI(request.get("message"));

        return ResponseEntity.ok(ApiResponse.success(response, "AI chat response"));
    }
}
