package vn.edu.iuh.fit.enums;

public enum ReportType {
    DAILY_SALES("Daily Sales Report"),
    WEEKLY_SALES("Weekly Sales Report"),
    MONTHLY_SALES("Monthly Sales Report"),
    PRODUCT_PERFORMANCE("Product Performance Report"),
    CUSTOMER_ANALYSIS("Customer Analysis Report"),
    INVENTORY_REPORT("Inventory Report");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
