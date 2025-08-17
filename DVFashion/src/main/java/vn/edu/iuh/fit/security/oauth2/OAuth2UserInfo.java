/*
 * @ {#} OAuth2UserInfo.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.oauth2;

import java.util.Map;

/*
 * @description: This class serves as an abstract base for OAuth2 user information
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
}
