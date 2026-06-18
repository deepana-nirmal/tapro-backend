package qr_ordering_system.service;

import java.util.List;

import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO dto);

    OrderResponseDTO getPublicOrderById(Long restaurantId, Long orderId);

    OrderResponseDTO getPublicOrderById(Long orderId);

    OrderResponseDTO getOrderById(Long orderId);

    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    List<OrderResponseDTO> getOrdersByTable(String tableNumber);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus);

    void cancelOrder(Long orderId);

    void deleteOrder(Long orderId);
}
