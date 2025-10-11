/*
 * @ {#} UserServiceImpl.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.Gender;
import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.exceptions.VerificationCodeException;
import vn.edu.iuh.fit.mappers.UserMapper;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.EmailService;
import vn.edu.iuh.fit.services.RoleService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/*
 * @description: Service implementation for managing user operations
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    private final UserMapper userMapper;

    private final EmailService emailService;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public UserResponse createCustomer(SignUpRequest signUpRequest) {
        String normalizedPhone = FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.phone());

        // Check for deleted account by email first
        Optional<User> deletedUserByEmail = userRepository.findDeletedByEmail(signUpRequest.email());

        // Check for deleted account by phone if not found by email
        Optional<User> deletedUserByPhone = userRepository.findDeletedByPhone(normalizedPhone);

        // If there's a deleted account, check if it can be restored
        User deletedUser = null;
        if (deletedUserByEmail.isPresent()) {
            deletedUser = deletedUserByEmail.get();
        } else if (deletedUserByPhone.isPresent()) {
            deletedUser = deletedUserByPhone.get();
        }

        if (deletedUser != null) {
            if (deletedUser.canRestore()) {
                // Account can be restored, do the restoration
                UserResponse restoredUser = restoreDeletedAccount(
                        signUpRequest.email(),
                        signUpRequest.phone(),
                        signUpRequest.password(),
                        signUpRequest.fullName()
                );
                return restoredUser;
            } else {
                // Account exists but cannot be restored yet (less than 30 days)
                long daysRemaining = 30 - ChronoUnit.DAYS.between(
                        deletedUser.getDeletedAt().toLocalDate(),
                        LocalDateTime.now().toLocalDate()
                );
                throw new IllegalStateException(
                        String.format("A deleted account with this email/phone exists. " +
                                "You can register again in %d days.", daysRemaining));
            }
        }

        // Check if email already exists
        if (existsByEmailAnIsDeletedFalse(signUpRequest.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        // Check if phone number already exists
        if (existsByPhoneAnIsDeletedFalse(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.phone()))) {
            throw new AlreadyExistsException("Phone number already exists");
        }

        // Retrieve the role for the customer
        Role role = roleService.findByName(UserRole.CUSTOMER);

        // Create a new User entity
        User user = new User();
        user.setEmail(signUpRequest.email());
        user.setPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.phone()));
        user.setFullName(signUpRequest.fullName());
        user.setPassword(passwordEncoder.encode(signUpRequest.password()));
        user.setTypeProviderAuths(new HashSet<>(Set.of(TypeProviderAuth.LOCAL)));
        user.setRoles(new HashSet<>(Set.of(role)));

        // Save the user to the repository
        User savedUser = userRepository.save(user);

        // Convert the saved User entity to UserResponse DTO
        return userMapper.toDto(savedUser);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserResponse getCurrentUser() {
        // Get the current authenticated user's username
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        System.out.println("Current username: " + username);

        // Check if the username is null, empty, or represents an anonymous user
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        // Normalize the phone number if the username is a phone number and not null/empty
        if (username != null && !username.trim().isEmpty()) {
            username = FormatPhoneNumber.normalizePhone(username);
        }

        // Find the user by username and check if they are active
        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Convert the User entity to UserResponse DTO
        return userMapper.toDto(user);
    }

    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return user;
    }

    @Override
    public void updatePassword(String phone, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + phone));

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findById(id);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Check if the username is null, empty, or represents an anonymous user
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        username = FormatPhoneNumber.normalizePhone(username);

        // Find the current authenticated user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Find the target user to be updated
        User targetUser = findById(id);

        // Check user roles and apply business rules
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == UserRole.ADMIN);
        boolean isCustomer = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == UserRole.CUSTOMER);

        if (isAdmin) {
            // Check if admin is trying to update other fields
            if (userRequest.fullName() != null && !userRequest.fullName().isBlank() ||
                    userRequest.email() != null && !userRequest.email().isBlank() ||
                    userRequest.phone() != null && !userRequest.phone().isBlank() ||
                    userRequest.gender() != null && !userRequest.gender().isBlank() ||
                    userRequest.dob() != null && !userRequest.dob().isBlank()) {

                throw new UnauthorizedException("Admin can only update the 'active' field");
            }

            // Admin can only update the 'active' field of any user
            if (userRequest.active() != null) {
                targetUser.setActive(userRequest.active());
            }
        } else if (isCustomer) {
            // Customer can only update their own information (except 'active' field)
            if (!currentUser.getId().equals(id)) {
                throw new UnauthorizedException("You are not authorized to update this user's information");
            }

            // Update user fields if they are provided in the request
            if (userRequest.fullName() != null && !userRequest.fullName().isBlank()) {
                targetUser.setFullName(userRequest.fullName());
            }

            if (userRequest.email() != null && !userRequest.email().isBlank()) {
                if (!userRequest.email().equals(currentUser.getEmail()) && existsByEmail(userRequest.email())) {
                    throw new AlreadyExistsException("Email already exists");
                }
                targetUser.setEmail(userRequest.email());
            }

            if (userRequest.phone() != null && !userRequest.phone().isBlank()) {
                String formattedPhone = FormatPhoneNumber.formatPhoneNumberTo84(userRequest.phone());
                if (!formattedPhone.equals(currentUser.getPhone()) && existsByPhone(formattedPhone)) {
                    throw new AlreadyExistsException("Phone number already exists");
                }
                targetUser.setPhone(formattedPhone);
            }
            if (userRequest.gender() != null && !userRequest.gender().isBlank()) {
                targetUser.setGender(Gender.valueOf(userRequest.gender()));
            }
            if (userRequest.dob() != null && !userRequest.dob().isBlank()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                targetUser.setDob(LocalDate.parse(userRequest.dob(), formatter));
            }
        } else {
            throw new UnauthorizedException("You are not authorized to update this user's information");
        }

        // Save the updated user entity
        User updatedUser = userRepository.save(targetUser);

        // Convert the updated User entity to UserResponse DTO
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserResponse createStaff(CreateStaffRequest request) {
        // Check if email or phone number already exists
        if (existsByEmail(request.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        if (existsByPhone(FormatPhoneNumber.formatPhoneNumberTo84(request.phone()))) {
            throw new AlreadyExistsException("Phone number already exists");
        }

        // Generate a verification code and set a default password
        String verificationCode = generateVerificationCode();
        String defaultPassword = "Staff@123";

        Role role = roleService.findByName(UserRole.STAFF);

        // Create a new User entity for the staff member
        User staff = new User();
        staff.setEmail(request.email());
        staff.setPhone(FormatPhoneNumber.formatPhoneNumberTo84(request.phone()));
        staff.setFullName(request.fullName());
        staff.setPassword(passwordEncoder.encode(defaultPassword));
        staff.setTypeProviderAuths(new HashSet<>(Set.of(TypeProviderAuth.LOCAL)));
        staff.setRoles(Set.of(role));
        staff.setVerificationCode(verificationCode);
        staff.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        staff.setVerified(false);
        staff.setActive(false);

        // Save the new staff member to the repository
        User savedStaff = userRepository.save(staff);

        // Send verification code email to the new staff member
        emailService.sendVerificationCode(request.email(), request.fullName(), defaultPassword, verificationCode);

        return userMapper.toDto(savedStaff);
    }

    @Override
    public UserResponse verifyStaff(VerifyStaffRequest request) {
        // Get the current authenticated user's username
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Check if the username is null, empty, or represents an anonymous user
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if the account is already verified
        if (user.isVerified()) {
            throw new VerificationCodeException("Account is already verified");
        }

        // Check if verification code is present
        if (user.getVerificationCode() == null || user.getVerificationCode().trim().isEmpty()) {
            throw new VerificationCodeException("No verification code found for this account");
        }

        // Check if verification code matches
        if (!request.verificationCode().equals(user.getVerificationCode())) {
            throw new VerificationCodeException("Invalid verification code");
        }

        // Check if verification code has expired
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeException("Verification code has expired");
        }

        // Update user
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setVerified(true);
        user.setActive(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        // Get the current authenticated user's username
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Check if the username is null, empty, or represents an anonymous user
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        username = FormatPhoneNumber.normalizePhone(username);

        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if the old password matches
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmailAnIsDeletedFalse(String email) {
        return userRepository.existsByEmailAndIsDeleteFalse(email);
    }

    @Override
    public boolean existsByPhoneAnIsDeletedFalse(String phone) {
        return userRepository.existsByPhoneAndIsDeleteFalse(phone);
    }

    @Override
    public void softDeleteAccount() {
        // Get the current authenticated user's username
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Check if the username is null, empty, or represents an anonymous user
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        // Normalize the phone number if the username is a phone number and not null/empty
        username = FormatPhoneNumber.normalizePhone(username);

        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if the account is already deleted
        if (user.isDeleted()) {
            throw new IllegalStateException("Account is already deleted");
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);

        userRepository.save(user);
    }

    @Override
    public UserResponse restoreDeletedAccount(String email, String phone, String password, String fullName) {
        String normalizedPhone = FormatPhoneNumber.formatPhoneNumberTo84(phone);

        // Check for deleted account by email first
        Optional<User> deletedUser = userRepository.findDeletedByEmail(email);

        // If not found by email, check by phone
        if (deletedUser.isEmpty()) {
            deletedUser = userRepository.findDeletedByPhone(normalizedPhone);
        }

        if (deletedUser.isPresent()) {
            User user = deletedUser.get();

            // Check if enough time has passed (30 days)
            if (!user.canRestore()) {
                throw new IllegalStateException("Cannot restore account yet. Must wait 30 days after deletion.");
            }

            // Restore the account with new data
            user.setDeleted(false);
            user.setDeletedAt(null);
            user.setActive(true);
            user.setEmail(email);
            user.setPhone(normalizedPhone);
            user.setFullName(fullName);
            user.setPassword(passwordEncoder.encode(password));
            user.setTypeProviderAuths(new HashSet<>(Set.of(TypeProviderAuth.LOCAL)));

            User restoredUser = userRepository.save(user);
            return userMapper.toDto(restoredUser);
        }

        return null;
    }

    // Helper method to generate a 6-digit verification code
    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
