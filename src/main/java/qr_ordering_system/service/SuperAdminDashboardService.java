package qr_ordering_system.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.ActivityItemResponseDTO;
import qr_ordering_system.dto.DashboardMetricResponseDTO;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public List<DashboardMetricResponseDTO> getMetrics() {
        long restaurantCount = restaurantRepository.count();
        long ownerCount = userRepository.countByRole(Role.OWNER);
        long userCount = userRepository.count();
        long orderCount = orderRepository.count();

        return List.of(
                new DashboardMetricResponseDTO("Total Restaurants", restaurantCount, "Active tenants across the platform", "blue"),
                new DashboardMetricResponseDTO("Total Owners", ownerCount, "Owner accounts currently assigned", "emerald"),
                new DashboardMetricResponseDTO("Total Users", userCount, "All platform users across roles", "amber"),
                new DashboardMetricResponseDTO("Total Orders", orderCount, "Orders flowing through all restaurants", "rose")
        );
    }

    public List<ActivityItemResponseDTO> getRecentActivities() {
        List<ActivityItemResponseDTO> activities = new ArrayList<>();

        restaurantRepository.findAll().stream()
                .sorted(Comparator.comparing(Restaurant::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .forEach(restaurant -> activities.add(new ActivityItemResponseDTO(
                        "restaurant-" + restaurant.getId(),
                        "Restaurant available",
                        restaurant.getName() + " is available in the platform workspace.",
                        java.time.LocalDateTime.now()
                )));

        userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId, Comparator.reverseOrder()))
                .limit(4)
                .forEach(user -> activities.add(new ActivityItemResponseDTO(
                        "user-" + user.getId(),
                        "User account active",
                        user.getEmail() + " is registered as " + user.getRole().name() + ".",
                        java.time.LocalDateTime.now()
                )));

        orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .forEach(order -> activities.add(new ActivityItemResponseDTO(
                        "order-" + order.getId(),
                        "Order update",
                        "Order #" + order.getId() + " is currently " + order.getStatus().name() + ".",
                        order.getCreatedAt()
                )));

        return activities.stream()
                .sorted(Comparator.comparing(ActivityItemResponseDTO::timestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();
    }
}
