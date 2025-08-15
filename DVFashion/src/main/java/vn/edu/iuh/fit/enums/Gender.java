/*
 * @ {#} Gender.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

public enum Gender {
    MALE("MALE"), FEMALE("FEMALE"), OTHER("OTHER");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
