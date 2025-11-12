/*
 * @ {#} ChatServiceImpl.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.CreateChatRoomRequest;
import vn.edu.iuh.fit.dtos.request.SendMessageRequest;
import vn.edu.iuh.fit.dtos.response.ChatMessageResponse;
import vn.edu.iuh.fit.dtos.response.ChatRoomResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.*;
import vn.edu.iuh.fit.enums.*;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.ChatMapper;
import vn.edu.iuh.fit.repositories.ChatMessageRepository;
import vn.edu.iuh.fit.repositories.ChatRoomRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ChatService;
import vn.edu.iuh.fit.services.FileUploadService;
import vn.edu.iuh.fit.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for chat functionalities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatMapper chatMapper;

    private final FileUploadService fileUploadService;

    private final UserService userService;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomResponse createGuestChatRoom(CreateChatRoomRequest request, Language language) {
        String roomCode = generateRoomCode();

        ChatRoom chatRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .type(ChatRoomType.GUEST)
                .guestName(request.guestName())
                .guestPhone(request.guestPhone())
                .status(ChatRoomStatus.ACTIVE)
                .unreadAdminCount(0)
                .unreadCustomerCount(0)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        log.info("Created guest chat room with code: {}", roomCode);

        return chatMapper.mapToResponse(chatRoom, language);
    }

    @Override
    @Transactional
    public ChatRoomResponse createCustomerChatRoom(Language language) {
        UserResponse currentUser = userService.getCurrentUser();
        User customer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + currentUser.getId()));

        // Check if customer already has an active chat room
        ChatRoom existingRoom = chatRoomRepository.findByCustomerAndStatus(customer, ChatRoomStatus.ACTIVE);
        if (existingRoom != null) {
            return chatMapper.mapToResponse(existingRoom, language);
        }

        String roomCode = generateRoomCode();

        ChatRoom chatRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .type(ChatRoomType.CUSTOMER)
                .customer(customer)
                .status(ChatRoomStatus.ACTIVE)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        log.info("Created customer chat room with code: {} for user: {}", roomCode, customer.getEmail());

        return chatMapper.mapToResponse(chatRoom, language);
    }

    @Override
    public ChatRoomResponse getChatRoomByCode(String roomCode, Language language) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Chat room not found with code: " + roomCode));

        return chatMapper.mapToResponse(chatRoom, language);
    }

    @Override
    public List<ChatMessageResponse> getChatMessages(String roomCode, int page, int size, Language language) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Chat room not found with code: " + roomCode));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChatMessage> messagesPage = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable);

        return messagesPage.getContent().stream()
                .map(message -> chatMapper.mapToMessageResponse(message, language))
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(String roomCode, SendMessageRequest request, Language language) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Chat room not found with code: " + roomCode));

        User user = null;
        MessageSender senderType;

        try {
            // If logged in
            UserResponse currentUser = userService.getCurrentUser();
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + currentUser.getId()));
            senderType = determineSenderType(user, chatRoom);

        } catch (Exception ex) {
            // If not logged in
            senderType = MessageSender.GUEST;
            log.warn("Guest user is sending message to room {}", roomCode);
        }


        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .senderType(senderType)
                .messageType(MessageType.TEXT)
                .content(request.content())
                .status(MessageStatus.SENT)
                .build();

        message = chatMessageRepository.save(message);

        // Update chat room
        updateChatRoomAfterMessage(chatRoom, message);

        log.info("Message sent in room {} by {}", roomCode, senderType);

        return chatMapper.mapToMessageResponse(message, language);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessageWithAttachment(String roomCode, String content, MultipartFile file, Language language) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Chat room not found with code: " + roomCode));

        // Validate file
        validateFile(file);

        // Upload file
        String fileUrl = fileUploadService.uploadFile(file, "chat-attachments");

        User user = null;
        MessageSender senderType;

        try {
            UserResponse currentUser = userService.getCurrentUser();
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + currentUser.getId()));
            senderType = determineSenderType(user, chatRoom);
        } catch (Exception e) {
            senderType = MessageSender.GUEST;
            log.warn("Guest user is uploading attachment in room {}", roomCode);
        }

        MessageType messageType = determineMessageType(file);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .senderType(senderType)
                .messageType(messageType)
                .content(content)
                .status(MessageStatus.SENT)
                .build();

        message = chatMessageRepository.save(message);

        // Create attachment
        ChatMessageAttachment attachment = ChatMessageAttachment.builder()
                .message(message)
                .type(determineAttachmentType(file))
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        message.getAttachments().add(attachment);

        // Update chat room
        updateChatRoomAfterMessage(chatRoom, message);

        log.info("Message with attachment sent in room {} by {}", roomCode, senderType);

        return chatMapper.mapToMessageResponse(message, language);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String roomCode) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new NotFoundException("Chat room not found with code: " + roomCode));

        User user = null;
        MessageSender senderType;

        try {
            UserResponse currentUser = userService.getCurrentUser();
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + currentUser.getId()));
            senderType = determineSenderType(user, chatRoom);
        } catch (Exception e) {
            senderType = MessageSender.GUEST;
            log.warn("Guest user is uploading attachment in room {}", roomCode);
        }

        if (senderType == MessageSender.ADMIN) {
            chatMessageRepository.markAsReadByAdmin(chatRoom.getId());
            chatRoom.setUnreadAdminCount(0);
        } else {
            chatMessageRepository.markAsReadByCustomer(chatRoom.getId());
            chatRoom.setUnreadCustomerCount(0);
        }

        chatRoomRepository.save(chatRoom);
    }

    @Override
    public List<ChatRoomResponse> getAdminChatRooms(int page, int size, Language language) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
        Page<ChatRoom> roomsPage = chatRoomRepository.findByStatusOrderByLastMessageAtDesc(ChatRoomStatus.ACTIVE, pageable);

        return roomsPage.getContent().stream()
                .map(room -> chatMapper.mapToResponse(room, language))
                .toList();
    }

    private String generateRoomCode() {
        return "CHAT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private MessageSender determineSenderType(User user, ChatRoom chatRoom) {
        if (user == null) {
            return MessageSender.GUEST;
        }

        Set<Role> roles = user.getRoles();

        boolean isAdmin = roles.stream()
                .anyMatch(role -> role.getName() == UserRole.ADMIN);
        if (isAdmin) {
            return MessageSender.ADMIN;
        }

        boolean isCustomer = roles.stream()
                .anyMatch(role -> role.getName() == UserRole.CUSTOMER);
        if (isCustomer) {
            return MessageSender.CUSTOMER;
        }

        return MessageSender.GUEST;
    }

    private MessageType determineMessageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MessageType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return MessageType.VIDEO;
            }
        }
        return MessageType.FILE;
    }

    private AttachmentType determineAttachmentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return AttachmentType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return AttachmentType.VIDEO;
            }
        }
        return AttachmentType.FILE;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size cannot exceed 10MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") &&
                !contentType.startsWith("video/") &&
                !contentType.equals("application/pdf"))) {
            throw new BadRequestException("Only image, video, and PDF files are allowed");
        }
    }

    private void updateChatRoomAfterMessage(ChatRoom chatRoom, ChatMessage message) {
        chatRoom.setLastMessageAt(LocalDateTime.now());

        // Update unread counts
        if (message.getSenderType() == MessageSender.ADMIN) {
            chatRoom.setUnreadCustomerCount(chatRoom.getUnreadCustomerCount() + 1);
        } else {
            chatRoom.setUnreadAdminCount(chatRoom.getUnreadAdminCount() + 1);
        }

        chatRoomRepository.save(chatRoom);
    }
}
