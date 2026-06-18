package qr_ordering_system.dto;

import qr_ordering_system.model.OrderStatus;

public class OrderStatusUpdateDTO {

    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}