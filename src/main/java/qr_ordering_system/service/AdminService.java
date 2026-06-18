package qr_ordering_system.service;

import java.time.LocalDate;
import java.util.List;

import qr_ordering_system.dto.SuperAdminUserRequestDTO;
import qr_ordering_system.dto.SuperAdminUserResponseDTO;
import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;

public interface AdminService {

    List<Restaurant> getAllRestaurants();

    void suspendRestaurant(Long id);

    void activateRestaurant(Long id);

    void deleteRestaurant(Long id);

    List<SuperAdminUserResponseDTO> getAllUsers();

    List<UsersByRestaurantResponseDTO> getUsersByRestaurant();

    SuperAdminUserResponseDTO createUser(SuperAdminUserRequestDTO request);

    SuperAdminUserResponseDTO updateUser(Long id, SuperAdminUserRequestDTO request);

    SuperAdminUserResponseDTO enableUser(Long id);

    SuperAdminUserResponseDTO disableUser(Long id);

    void deleteUser(Long id);

    List<OrderResponseDTO> getRestaurantActiveOrders(Long restaurantId);

    List<OrderResponseDTO> getRestaurantPastOrders(
            Long restaurantId,
            LocalDate from,
            LocalDate to,
            OrderStatus status,
            String tableNumber
    );

    List<OwnerStaffUserResponse> getRestaurantUsers(Long restaurantId);

    OwnerAnalyticsResponse getRestaurantAnalytics(Long restaurantId);

    List<MenuItemResponseDTO> getRestaurantMenuItems(Long restaurantId, String currentUserEmail);

    List<CategoryResponseDTO> getRestaurantCategories(Long restaurantId, String currentUserEmail);

    List<TableResponseDTO> getRestaurantTables(Long restaurantId, String currentUserEmail);
}
