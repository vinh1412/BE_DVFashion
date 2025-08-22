/*
 * @ {#} UserMapper.java   1.0     23/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;
/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   23/08/2025
 * @version:    1.0
 */

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {
    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user the User entity to map
     * @return the mapped UserResponse DTO
     */
    @Mapping(target = "roles", source = "roles") // map roles to a list of strings
    UserResponse toDto(User user);

    /**
     * Maps a set of roles to a list of role names prefixed with "ROLE_".
     *
     * @param roles the set of roles to map
     * @return a list of role names as strings, or an empty list if the input is null
     */
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(role -> "ROLE_" + role.getName().name()) // convert to String
                .toList();
    }
}
