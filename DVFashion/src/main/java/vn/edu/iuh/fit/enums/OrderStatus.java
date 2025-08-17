package vn.edu.iuh.fit.enums;

public enum OrderStatus {
    PENDING("Pending"), CONFIRMED("Confirmed"), PROCESSING("Processing"), SHIPPED("Shipped"), DELIVERED("Delivered"), CANCELED("Canceled"), RETURNED("Returned");
    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
