/*
 * @ {#} CookieUtils.java   1.0     17/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import vn.edu.iuh.fit.config.CookieEnvConfig;

/*
 * @description: Utility class for managing cookies in HTTP responses
 * @author: Tran Hien Vinh
 * @date:   17/08/2025
 * @version:    1.0
 */
@UtilityClass
public class CookieUtils {
    private static boolean IS_PROD = false;

    public static void init(CookieEnvConfig config) {
        IS_PROD = config.isProd();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);

        if (IS_PROD) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
            cookie.setDomain("devcode.id.vn");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Strict");
        }

        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        if (IS_PROD) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
            cookie.setDomain("devcode.id.vn");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Strict");
        }
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
