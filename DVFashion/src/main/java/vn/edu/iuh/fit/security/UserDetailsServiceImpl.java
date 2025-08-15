/*
 * @ {#} UserDetailsServiceImpl.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

/*
 * @description: Service implementation for loading user details from the database
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    // This method is called by Spring Security to load user details by username
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String value = FormatPhoneNumber.normalizePhone(username);
        User user = userRepository.findByUsernameAndActiveTrue(value)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + value));

        return UserDetailsImpl.build(user);
    }
}
