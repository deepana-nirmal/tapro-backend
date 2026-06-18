package qr_ordering_system.service;

import java.util.List;

import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;

public interface KitchenService {

    List<OrderResponseDTO> getKitchenOrders();

    List<OrderResponseDTO> getPastOrders();

    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status);
}
