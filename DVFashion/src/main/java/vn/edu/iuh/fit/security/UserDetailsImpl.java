/*
 * @ {#} UserDetailsImpl.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.fit.entities.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description: This class implements UserDetails interface to provide user details for Spring Security
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@Builder
public class UserDetailsImpl implements UserDetails {
    private Long id;

    private String email;

    private String phone;

    private String loginUsername;

    @JsonIgnore
    private String password;

    private boolean active;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String email, String phone, String loginUsername, String password,
                           boolean active, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.loginUsername = loginUsername;
        this.password = password;
        this.active = active;
        this.authorities = authorities;
    }

    // Factory method to create UserDetailsImpl from User entity
    public static UserDetailsImpl build(User user, String username) {
        // Generate authorities from user roles
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        //  Return a new instance of UserDetailsImpl
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                username,
                user.getPassword(),
                user.isActive(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginUsername;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLoginUsername() {
        return loginUsername;
    }
}
