/*
 * @ {#} RoleService.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.enums.UserRole;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public interface RoleService {
    /**
     * Finds a role by its name.
     *
     * @param role the UserRole enum representing the role to find
     * @return the Role entity corresponding to the specified UserRole, or exception if not found
     */
    Role findByName(UserRole role);

    /**
     * Saves a role entity to the database.
     *
     * @param role the Role entity to save
     * @return the saved Role entity
     */
    Role save(Role role);
}
