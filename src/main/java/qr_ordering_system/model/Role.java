package qr_ordering_system.model;

public enum Role {

    SUPER_ADMIN,   // 👑 YOU (platform owner)

    ADMIN,         // restaurant admin / manager
    OWNER,

    STAFF,
    KITCHEN,
    CASHIER,

    CUSTOMER
}