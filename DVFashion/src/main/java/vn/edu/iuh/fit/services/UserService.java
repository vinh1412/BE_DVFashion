/*
 * @ {#} UserService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.User;

import java.util.List;

/*
 * @description: Service interface for managing user operations
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public interface UserService {
    /**
     * Check if a user exists by their email.
     *
     * @param email the email to check
     * @return true if a user with the specified email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists by their phone number.
     *
     * @param phone the phone number to check
     * @return true if a user with the specified phone number exists, false otherwise
     */
    boolean existsByPhone(String phone);

    /**
     * Create a new customer.
     *
     * @param signUpRequest the request containing user details for sign up
     * @return the UserResponse containing the created user's details
     */
    UserResponse createCustomer(SignUpRequest signUpRequest);

    /**
     * Find a user by their ID.
     *
     * @param id the ID of the user
     * @return the User entity with the specified ID, or null if not found
     */
    User findById(Long id);

    /**
     * Check if a user exists by their username (email or phone).
     *
     * @param username the username to check
     * @return true if a user with the specified username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Get the current logged-in user.
     *
     * @return the UserResponse representing the current user
     */
    UserResponse getCurrentUser();

    /**
     * Find a user by their email.
     *
     * @param email the email of the user
     * @return the User entity with the specified email, or null if not found
     */
    User findByEmail(String email);

    /**
     * Update the password for a user identified by their phone number.
     *
     * @param phone
     * @param newPassword
     */
    void updatePassword(String phone, String newPassword);

    /**
     * Get a user by their ID.
     *
     * @param id the ID of the user
     * @return the UserResponse representing the user with the specified ID
     */
    UserResponse getUserById(Long id);

    /**
     * Update user information for a user identified by their ID.
     *
     * @param id           the ID of the user to update
     * @param userRequest the UserResponse containing updated user details
     * @return the updated UserResponse
     */
    UserResponse updateUser(Long id, UserRequest userRequest);

    /**
     * Create a new staff member.
     *
     * @param request the CreateStaffRequest containing staff details
     * @return the UserResponse containing the created staff's details
     */
    UserResponse createStaff(CreateStaffRequest request);

    /**
     * Verify a staff member's account.
     *
     * @param request the VerifyStaffRequest containing verification details
     * @return the UserResponse containing the verified staff's details
     */
    UserResponse verifyStaff(VerifyStaffRequest request);

    /**
     * Retrieve all users.
     *
     * @return a list of UserResponse representing all users
     */
    List<UserResponse> getAllUsers();

    /**
     * Change the password for the current logged-in user.
     *
     * @param request the ChangePasswordRequest containing the new password details
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Check if a user exists by their email and is not deleted.
     *
     * @param email the email to check
     * @return true if a user with the specified email exists and is not deleted, false otherwise
     */
    boolean existsByEmailAnIsDeletedFalse(String email);

    /**
     * Check if a user exists by their phone and is not deleted.
     *
     * @param phone the phone number to check
     * @return true if a user with the specified phone number exists and is not deleted, false otherwise
     */
    boolean existsByPhoneAnIsDeletedFalse(String phone);

    /**
     * Soft delete the current logged-in user's account.
     * The account can be restored within 30 days by re-registering.
     */
    void softDeleteAccount();

    /**
     * Restore a deleted account using email, phone, password, and full name.
     *
     * @param email    the email of the deleted account
     * @param phone    the phone number of the deleted account
     * @param password the new password for the restored account
     * @param fullName the full name for the restored account
     * @return the UserResponse containing the restored user's details
     */
    UserResponse restoreDeletedAccount(String email, String phone, String password, String fullName);
}
