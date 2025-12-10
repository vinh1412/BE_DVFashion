/*
 * @ {#} CookieEnvConfig.java   1.0     04/12/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/*
 * @description: Configuration class to manage cookie environment settings
 * @author: Tran Hien Vinh
 * @date:   04/12/2025
 * @version:    1.0
 */
@Configuration
@Getter
@Slf4j
public class CookieEnvConfig {

    @Value("${app.cookie.env:dev}")  // default = dev
    private String appEnv;

    public boolean isProd() {
        boolean isProd = "prod".equalsIgnoreCase(appEnv);
        log.info("CookieUtils initialized. ENV = " + appEnv + ", isProd=" + isProd);
        return isProd;
    }
}
