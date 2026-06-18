package qr_ordering_system.service;

import java.time.LocalDate;
import java.util.List;

import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;

public interface OwnerService {

    List<OrderResponseDTO> getAllOrders(Long restaurantId, String currentUserEmail);

    List<OrderResponseDTO> getActiveOrders(String currentUserEmail);

    List<OrderResponseDTO> getPastOrders(
            String currentUserEmail,
            LocalDate from,
            LocalDate to,
            OrderStatus status,
            String tableNumber
    );

    List<OrderResponseDTO> getOrdersByStatus(Long restaurantId, OrderStatus status, String currentUserEmail);

    OrderResponseDTO getOrder(Long orderId, String currentUserEmail);

    OrderResponseDTO getOrderById(Long orderId); // ✅ for old tests compatibility

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status, String currentUserEmail);

    List<OwnerStaffUserResponse> getManagedStaff(String currentUserEmail);

    OwnerStaffUserResponse updateStaffStatus(Long userId, boolean enabled, String currentUserEmail);

    void resetStaffPassword(Long userId, String currentUserEmail);

    OwnerAnalyticsResponse getAnalytics(String currentUserEmail);
}
