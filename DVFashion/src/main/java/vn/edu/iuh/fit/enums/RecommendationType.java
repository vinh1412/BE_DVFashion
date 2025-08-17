package vn.edu.iuh.fit.enums;

public enum RecommendationType {
    COLLABORATIVE_FILTERING("Collaborative Filtering"),
    CONTENT_BASED("Content Based"),
    POPULAR_ITEMS("Popular Items"),
    CROSS_SELL("Cross Sell"),
    UP_SELL("Up Sell");

    private final String description;

    RecommendationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
