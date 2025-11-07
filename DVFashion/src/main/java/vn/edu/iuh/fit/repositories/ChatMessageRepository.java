/*
 * @ {#} ChatMessageRepository.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ChatMessage;
import vn.edu.iuh.fit.entities.ChatRoom;

/*
 * @description: Repository interface for managing ChatMessage entities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isReadByAdmin = true WHERE m.chatRoom.id = :chatRoomId AND m.isReadByAdmin = false")
    void markAsReadByAdmin(Long chatRoomId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isReadByCustomer = true WHERE m.chatRoom.id = :chatRoomId AND m.isReadByCustomer = false")
    void markAsReadByCustomer(Long chatRoomId);
}
