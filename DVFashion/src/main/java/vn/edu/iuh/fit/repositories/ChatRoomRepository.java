/*
 * @ {#} ChatRoomRepository.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ChatRoom;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.ChatRoomStatus;

import java.util.Optional;

/*
 * @description: Repository interface for managing ChatRoom entities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomCode(String roomCode);
    ChatRoom findByCustomerAndStatus(User customer, ChatRoomStatus status);
    Page<ChatRoom> findByStatusOrderByLastMessageAtDesc(ChatRoomStatus status, Pageable pageable);
}
