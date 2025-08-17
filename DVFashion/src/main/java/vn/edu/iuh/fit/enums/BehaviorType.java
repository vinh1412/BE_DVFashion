package vn.edu.iuh.fit.enums;

public enum BehaviorType {
    VIEW("VIEW"), ADD_TO_CART("ADD_TO_CART"), PURCHASE("PURCHASE"), LIKE("LIKE"), SHARE("SHARE"), SEARCH("SEARCH");

    private final String value;

    BehaviorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
