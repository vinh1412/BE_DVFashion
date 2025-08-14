/*
 * @ {#} FormatPhoneNumber.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
public class FormatPhoneNumber {
    /**
     * Formats a phone number to the international format for Vietnam (+84).
     *
     * @param phoneNumber the phone number to format
     * @return the formatted phone number
     */
    public static String formatPhoneNumberTo84(String phoneNumber) {
        if (phoneNumber.startsWith("0")) {
            return "+84" + phoneNumber.substring(1);
        }
        if(phoneNumber.startsWith("+840")) {
            return "+84" + phoneNumber.substring(4);
        }
        if(phoneNumber.startsWith("84")) {

            return "+" + phoneNumber;
        }

        return phoneNumber;
    }
}
