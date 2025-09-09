/*
 * @ {#} LanguageUtils.java   1.0     10/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.iuh.fit.enums.Language;

/*
 * @description: Utility class for language-related operations
 * @author: Tran Hien Vinh
 * @date:   10/09/2025
 * @version:    1.0
 */
@UtilityClass
public class LanguageUtils {
    /**
     * Get the current language from the HTTP request.
     * The method checks for a "lang" query parameter first,
     * then the "Accept-Language" header, and finally a custom "X-Language" header.
     * If none are found, it defaults to Vietnamese (VI).
     *
     * @return the current Language enum
     */
    public static Language getCurrentLanguage() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Language.VI; // Default fallback
        }

        HttpServletRequest request = attributes.getRequest();

        // 1. Check query parameter first
        String langParam = request.getParameter("lang");
        if (langParam != null) {
            return parseLanguage(langParam);
        }

        // 2. Check Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            return parseAcceptLanguageHeader(acceptLanguage);
        }

        // 3. Check custom header
        String customLang = request.getHeader("X-Language");
        if (customLang != null) {
            return parseLanguage(customLang);
        }

        return Language.VI; // Default fallback
    }

    /**
     * Parse a language string to the corresponding Language enum.
     *
     * @param lang the language string (e.g., "en", "vi", "english", "vietnamese")
     * @return the corresponding Language enum, or VI as default
     */
    private static Language parseLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return Language.VI;
        }

        String normalizedLang = lang.toLowerCase().trim();

        return switch (normalizedLang) {
            case "en", "eng", "english" -> Language.EN;
            case "vi", "vie", "vietnamese" ->Language.VI;
            default ->Language.VI; // Default fallback
        };
    }

    /**
     * Parse the Accept-Language header to determine the preferred language.
     *
     * @param acceptLanguage the Accept-Language header value
     * @return the corresponding Language enum, or VI as default
     */
    private static Language parseAcceptLanguageHeader(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty()) {
            return Language.VI;
        }

        // Parse Accept-Language header (e.g., "en-US,en;q=0.9,vi;q=0.8")
        String[] languages = acceptLanguage.split(",");
        for (String lang : languages) {
            String cleanLang = lang.split(";")[0].trim(); // Remove quality value
            if (cleanLang.startsWith("en")) {
                return Language.EN;
            } else if (cleanLang.startsWith("vi")) {
                return Language.VI;
            }
        }

        return Language.VI; // Default fallback
    }
}
