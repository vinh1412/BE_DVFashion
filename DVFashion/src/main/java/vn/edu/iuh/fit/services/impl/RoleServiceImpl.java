/*
 * @ {#} RoleServiceImpl.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.RoleRepository;
import vn.edu.iuh.fit.services.RoleService;

import java.util.Optional;

/*
 * @description: Service implementation for managing user roles
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role findByName(UserRole role) {
        Optional<Role> optionalRole = roleRepository.findByName(role);
        if (optionalRole.isPresent()) {
            return optionalRole.get();
        } else {
            throw new NotFoundException("Role not found: " + role);
        }
    }

    @Override
    public Role save(Role role) {
        if (role == null || role.getName() == null) {
            throw new IllegalArgumentException("Role or role name cannot be null");
        }
        return roleRepository.save(role);
    }
}
