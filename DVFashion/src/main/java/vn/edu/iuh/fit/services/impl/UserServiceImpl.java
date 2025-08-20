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
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.RoleService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

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

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public User createCustomer(SignUpRequest signUpRequest) {
        if (existsByEmail(signUpRequest.getEmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        if (existsByPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.getPhone()))) {
            throw new AlreadyExistsException("Phone number already exists");
        }

        Role role = roleService.findByName(UserRole.CUSTOMER);

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.getPhone()));
        user.setFullName(signUpRequest.getFullName());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
//        user.setTypeProviderAuth(TypeProviderAuth.LOCAL);
        user.setRoles(Set.of(role));

        return userRepository.save(user);
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
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        System.out.println("Current username: " + username);
        if (username == null || username.isEmpty() || username.equals("anonymousUser")) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Optional<User> userOptional = userRepository.findByUsernameAndActiveTrue(username);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        User user = userOptional.get();

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .dob(user.getDob() != null ? user.getDob() : null)
                .gender(user.getGender() != null ? user.getGender() : null)
                .roles(user.getRoles().stream()
                        .map(role -> "ROLE_" +role.getName().name())
                        .toList())
                .build();

        return userResponse;
    }
}
