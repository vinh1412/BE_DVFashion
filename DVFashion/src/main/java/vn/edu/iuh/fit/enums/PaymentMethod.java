package vn.edu.iuh.fit.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on Delivery"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer");
    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
