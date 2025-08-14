package vn.edu.iuh.fit.enums;

public enum TypeProviderAuth {
    GOOGLE("GOOGLE"),
    LOCAL("LOCAL");

    private final String value;

    TypeProviderAuth(String value) {
        this.value = value;
    }

    public String getType() {
        return value;
    }
}
