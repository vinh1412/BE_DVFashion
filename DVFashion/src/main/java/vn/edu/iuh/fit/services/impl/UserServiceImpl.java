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
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.request.UserRequest;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.Gender;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.mappers.UserMapper;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.RoleService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        // Check if email already exists
        if (existsByEmail(signUpRequest.email())) {
            throw new AlreadyExistsException("Email already exists");
        }

        // Check if phone number already exists
        if (existsByPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.phone()))) {
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
//        user.setTypeProviderAuth(TypeProviderAuth.LOCAL);
        user.setRoles(Set.of(role));

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

        // Normalize the phone number if the username is a phone number
        username = FormatPhoneNumber.normalizePhone(username);

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
        User user = findById(id);

        // Update user fields if they are provided in the request
        if (userRequest.fullName() != null) {
            user.setFullName(userRequest.fullName());
        }

        if (userRequest.email() != null) {
            if (!userRequest.email().equals(user.getEmail()) && existsByEmail(userRequest.email())) {
                throw new AlreadyExistsException("Email already exists");
            }
            user.setEmail(userRequest.email());
        }

        if (userRequest.phone() != null) {
            String formattedPhone = FormatPhoneNumber.formatPhoneNumberTo84(userRequest.phone() );
            if (!formattedPhone.equals(user.getPhone()) && existsByPhone(formattedPhone)) {
                throw new AlreadyExistsException("Phone number already exists");
            }
            user.setPhone(formattedPhone);
        }
        if (userRequest.gender() != null) {
            user.setGender(Gender.valueOf(userRequest.gender()));
        }
        if (userRequest.dob() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            user.setDob(LocalDate.parse(userRequest.dob(), formatter));
        }

        // Save the updated user entity
        User updatedUser = userRepository.save(user);

        // Convert the updated User entity to UserResponse DTO
        return userMapper.toDto(updatedUser);
    }
}
