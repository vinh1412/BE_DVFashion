package vn.edu.iuh.fit.enums;

import vn.edu.iuh.fit.exceptions.NotFoundEnumValueException;

public enum PromotionType {
    PERCENTAGE("Percentage Discount"),
    FIXED_AMOUNT("Fixed Amount Discount"),
    FREE_SHIPPING("Free Shipping"),
    BUY_ONE_GET_ONE("Buy One Get One Free");

    private final String description;

    PromotionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts a string to a PromotionType enum, ignoring case.
     *
     * @param value the string representation of the promotion type
     * @return the corresponding PromotionType enum
     * @throws NotFoundEnumValueException if no matching enum is found
     */
    public static PromotionType fromString(String value) {
        for (PromotionType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new NotFoundEnumValueException("No enum constant for value: " + value);
    }
}
