package trantantai.trantantai.constants;

public enum PaymentStatus {
    PENDING_PAYMENT,  // Waiting for MoMo payment
    PAID,             // Payment confirmed
    PAYMENT_FAILED,   // Payment failed/cancelled
    COD_PENDING       // COD - waiting for delivery (default for COD orders)
}
