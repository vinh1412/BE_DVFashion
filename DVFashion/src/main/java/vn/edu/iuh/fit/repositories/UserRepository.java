/*
 * @ {#} UserRepository.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.User;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their email.
     *
     * @param email the username of the user
     * @return the user with the specified username, or null if not found
     */
    User findByEmail(String email);

    /**
     * Check if a user exists by their email.
     *
     * @param email the username to check
     * @return true if a user with the specified email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists by their phone.
     *
     * @param phone the phone number to check
     * @return true if a user with the specified phone number exists, false otherwise
     */
    boolean existsByPhone(String phone);
}
