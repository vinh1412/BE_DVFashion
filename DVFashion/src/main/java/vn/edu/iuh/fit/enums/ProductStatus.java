package vn.edu.iuh.fit.enums;

public enum ProductStatus {
    ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), OUT_OF_STOCK("OUT_OF_STOCK"), DISCONTINUED("DISCONTINUED");
    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
