/*
 * @ {#} CustomOAuth2UserService.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.iuh.fit.entities.Role;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.enums.UserRole;
import vn.edu.iuh.fit.exceptions.OAuth2AuthenticationProcessingException;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.RoleService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/*
 * @description: Service for handling OAuth2 user authentication and registration
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        TypeProviderAuth currentProvider = TypeProviderAuth.valueOf(
                oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        // Check if the user already exists in the database
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // If the user is already registered with the current provider, update their information
            if (!user.getTypeProviderAuths().contains(currentProvider)) {
                user.getTypeProviderAuths().add(currentProvider);
            }

            // Update user information with the new OAuth2 user info
            user = updateExistingUser(user, oAuth2UserInfo, currentProvider);
        } else {
            // If the user does not exist, register a new user
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo, currentProvider);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    // Registers a new user with the OAuth2 provider information
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo,  TypeProviderAuth currentProvider) {
        User user = new User();

        user.setTypeProviderAuths(new HashSet<>(Set.of(currentProvider)));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setFullName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());

        // Set default role as CUSTOMER
        Role customerRole = roleService.findByName(UserRole.CUSTOMER);
        user.setRoles(Set.of(customerRole));

        return userRepository.save(user);
    }

    // Updates an existing user with the new OAuth2 user information
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, TypeProviderAuth currentProvider) {;
        existingUser.setProviderId(oAuth2UserInfo.getId());

        return userRepository.save(existingUser);
    }
}
