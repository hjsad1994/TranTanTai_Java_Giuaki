package trantantai.trantantai.constants;

/**
 * OrderStatus enum for order lifecycle management.
 * Separate from PaymentStatus to track fulfillment independently.
 */
public enum OrderStatus {
    PROCESSING("Đang xử lý"),
    SHIPPED("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
