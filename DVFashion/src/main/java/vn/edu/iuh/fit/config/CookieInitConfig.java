/*
 * @ {#} CookieInitConfig.java   1.0     05/12/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import vn.edu.iuh.fit.utils.CookieUtils;

/*
 * @description: Configuration class to initialize cookie settings
 * @author: Tran Hien Vinh
 * @date:   05/12/2025
 * @version:    1.0
 */
@Configuration
@RequiredArgsConstructor
public class CookieInitConfig {

    private final CookieEnvConfig cookieEnvConfig;

    @PostConstruct
    public void init() {
        CookieUtils.init(cookieEnvConfig);
    }
}
