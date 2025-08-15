/*
 * @ {#} FormatPhoneNumber.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

/*
 * @description: Utility class for formatting and normalizing phone numbers in Vietnam
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

    /**
     * Formats a phone number to the local format for Vietnam (without +84).
     *
     * @param phoneNumber the phone number to format
     * @return the formatted phone number
     */
    public static String formatPhoneNumberToLocal(String phoneNumber) {
        if (phoneNumber.startsWith("+84")) {
            return "0" + phoneNumber.substring(3);
        }
        if (phoneNumber.startsWith("84")) {
            return "0" + phoneNumber.substring(2);
        }
        if (phoneNumber.startsWith("0")) {
            return phoneNumber;
        }
        return "0" + phoneNumber;
    }

    /**
     * Normalizes a phone number to the international format for Vietnam (+84).
     *
     * @param input the phone number to normalize
     * @return the normalized phone number
     */
    public static String normalizePhone(String input){
        if (input.matches("^0\\d{9}$")) {
            return "+84" + input.substring(1);
        } else if (input.matches("^\\+840\\d{9}$")) {
            return "+84" + input.substring(4);
        } else if (input.matches("^84\\d{9}$")) {
            return "+" + input;
        }
        return input;
    }
}
