/*
 * @ {#} RoleRepository.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.enums.UserRole;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Find a role by its name.
     *
     * @param name the name of the role
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(UserRole name);
}
