/*
 * @ {#} UserRepository.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.User;

import java.util.Optional;

/*
 * @description: Repository interface for managing users in the system
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
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

    /**
     * Find a user by email or phone and ensure the user is active.
     *
     * @param username
     * @return an Optional containing the User if found and active, or empty if not found or inactive
     */
    @Query("SELECT u FROM User u WHERE (u.email = :username OR u.phone = :username) AND u.active = true AND u.isDeleted = false")
    Optional<User> findByUsernameAndActiveTrue(@Param("username") String username);

    /**
     * Check if a user exists by their username (email or phone).
     *
     * @param username the username to check
     * @return true if a user with the specified username exists, false otherwise
     */
    @Query(value = """
         SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END 
         FROM User u 
         WHERE (u.email = :username OR u.phone = :username) AND u.active = true""")
    boolean existsByUsername(String username);

    /**
     * Find a user by their email.
     *
     * @param email the email to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their phone number.
     *
     * @param phone the phone number to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    Optional<User> findByPhone(String phone);

    /**
     * Find a user by email or phone.
     *
     * @param username the email or phone to search for
     * @return an Optional containing the User if found, or empty if not found
     */
    @Query("SELECT u FROM User u WHERE (u.email = :username OR u.phone = :username)")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Check if a user exists by their email and is not deleted.
     *
     * @param email the email to check
     * @return true if a user with the specified email exists and is not deleted, false otherwise
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    boolean existsByEmailAndIsDeleteFalse(String email);

    /**
     * Check if a user exists by their phone and is not deleted.
     *
     * @param phone the phone number to check
     * @return true if a user with the specified phone number exists and is not deleted, false otherwise
     */
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.isDeleted = false")
    boolean existsByPhoneAndIsDeleteFalse(String phone);

    /**
     * Find a deleted user by their email.
     *
     * @param email the email to search for
     * @return an Optional containing the User if found and is deleted, or empty if not found or not deleted
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = true")
    Optional<User> findDeletedByEmail(@Param("email") String email);

    /**
     * Find a deleted user by their phone number.
     *
     * @param phone the phone number to search for
     * @return an Optional containing the User if found and is deleted, or empty if not found or not deleted
     */
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.isDeleted = true")
    Optional<User> findDeletedByPhone(@Param("phone") String phone);
}
