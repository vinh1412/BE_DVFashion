/*
 * @ {#} GoogleOAuth2UserInfo.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.oauth2;

import java.util.Map;

/*
 * @description: This class represents the user information retrieved from Google OAuth2 authentication.
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
}
