package vn.edu.iuh.fit.enums;

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
}
