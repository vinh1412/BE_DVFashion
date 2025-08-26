/*
 * @ {#} TextUtils.java   1.0     25/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

/*
 * @description: Utility class for text manipulation, including a method to remove trailing dots from strings
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */
public class TextUtils {
    /**
     * Removes a trailing dot from the given text if it exists.
     *
     * @param text the input text
     * @return the text without a trailing dot, or null if the input was null
     */
    public static String removeTrailingDot(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        return text;
    }
}
