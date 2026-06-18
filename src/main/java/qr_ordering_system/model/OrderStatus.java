package qr_ordering_system.model;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED // 🔥 ADD THIS (important for SaaS + order lifecycle)
}