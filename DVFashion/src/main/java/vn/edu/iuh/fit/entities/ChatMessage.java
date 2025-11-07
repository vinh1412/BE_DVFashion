/*
 * @ {#} ChatMessage.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.iuh.fit.enums.MessageSender;
import vn.edu.iuh.fit.enums.MessageStatus;
import vn.edu.iuh.fit.enums.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description: Entity representing a chat message
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; // null if senderType is GUEST

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private MessageSender senderType; // CUSTOMER, GUEST, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "is_read_by_customer")
    @Builder.Default
    private Boolean isReadByCustomer = false;

    @Column(name = "is_read_by_admin")
    @Builder.Default
    private Boolean isReadByAdmin = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessageAttachment> attachments = new ArrayList<>();
}