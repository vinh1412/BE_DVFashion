/*
 * @ {#} UserServiceImpl.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.RoleService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.util.Set;

/*
 * @description:
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
        if (existsByPhone(signUpRequest.getPhone())) {
            throw new AlreadyExistsException("Phone number already exists");
        }

        Role role = roleService.findByName(UserRole.CUSTOMER);

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPhone(FormatPhoneNumber.formatPhoneNumberTo84(signUpRequest.getPhone()));
        user.setFullName(signUpRequest.getFullName());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setTypeProviderAuth(TypeProviderAuth.LOCAL);
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }
}
