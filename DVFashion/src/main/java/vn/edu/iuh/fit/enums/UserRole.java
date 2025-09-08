package vn.edu.iuh.fit.enums;

public enum UserRole {
    ADMIN("ADMIN"), CUSTOMER("CUSTOMER");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
