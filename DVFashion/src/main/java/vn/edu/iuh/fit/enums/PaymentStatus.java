package vn.edu.iuh.fit.enums;

public enum PaymentStatus {
    PENDING("Pending"), COMPLETED("Completed"), FAILED("Failed"), REFUNDED("Refunded"), CANCELED("Canceled");
    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
