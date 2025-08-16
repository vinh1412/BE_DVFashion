/*
 * @ {#} OAuth2UserInfoFactory.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.oauth2;

import vn.edu.iuh.fit.enums.TypeProviderAuth;
import vn.edu.iuh.fit.exceptions.OAuth2AuthenticationProcessingException;

import java.util.Map;

/*
 * @description: Factory class for creating OAuth2UserInfo instances based on the provider type
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(TypeProviderAuth.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
