package qr_ordering_system.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerItemSalesResponse;
import qr_ordering_system.dto.OwnerMetricPeriodValue;
import qr_ordering_system.dto.OwnerPeakHourResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.OrderItemResponseDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.EmailService;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OwnerService;
import qr_ordering_system.service.OrderStatusTransitionValidator;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.RestaurantCurrencySupport;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnerServiceImpl implements OwnerService {

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.PREPARING,
            OrderStatus.READY
    );
    private static final List<OrderStatus> PAST_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
    );

    private static final String RESET_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RestaurantAccessGuard restaurantAccessGuard;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final OrderStatusTransitionValidator orderStatusTransitionValidator;

    @Override
    public List<OrderResponseDTO> getAllOrders(Long restaurantId, String currentUserEmail) {
        authorizeOrderAccess(restaurantId, currentUserEmail);

        return orderRepository.findByTenantId(restaurantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getActiveOrders(String currentUserEmail) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);

        return orderRepository.findOrdersForRestaurant(
                        restaurant.getId(),
                        ACTIVE_ORDER_STATUSES,
                        null,
                        null
                )
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getPastOrders(
            String currentUserEmail,
            LocalDate from,
            LocalDate to,
            OrderStatus status,
            String tableNumber) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);

        List<OrderStatus> statuses = status != null ? List.of(status) : PAST_ORDER_STATUSES;
        validatePastOrderStatuses(statuses);

        return filterByTableNumber(orderRepository.findOrdersForRestaurant(
                        restaurant.getId(),
                        statuses,
                        from != null ? from.atStartOfDay() : null,
                        to != null ? to.plusDays(1).atStartOfDay() : null
                ), tableNumber)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByStatus(Long restaurantId, OrderStatus status, String currentUserEmail) {
        authorizeOrderAccess(restaurantId, currentUserEmail);

        return orderRepository.findByTenantIdAndStatus(restaurantId, status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrder(Long orderId, String currentUserEmail) {
        return getOrderById(orderId, currentUserEmail);
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        return getOrderById(orderId, null);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status, String currentUserEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        authorizeOrderAccess(order.getTenantId(), currentUserEmail);
        orderStatusTransitionValidator.validateKitchenOrOwnerTransition(order.getStatus(), status);

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        OrderResponseDTO response = mapToDTO(updated);
        String message = "Order #" + orderId + " is now " + status;

        messagingTemplate.convertAndSend("/topic/orders/" + order.getTenantId(), response);
        messagingTemplate.convertAndSend("/topic/notifications/" + order.getTenantId(), message);
        notificationService.create(message, "ORDER");

        return response;
    }

    private OrderResponseDTO getOrderById(Long orderId, String currentUserEmail) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (currentUserEmail != null) {
            authorizeOrderAccess(order.getTenantId(), currentUserEmail);
        }

        return mapToDTO(order);
    }

    @Override
    public List<OwnerStaffUserResponse> getManagedStaff(String currentUserEmail) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);

        return userRepository.findByRestaurant_IdAndRoleIn(restaurant.getId(), List.of(Role.STAFF, Role.KITCHEN))
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OwnerStaffUserResponse updateStaffStatus(Long userId, boolean enabled, String currentUserEmail) {
        User staffUser = getManagedStaffUser(userId, currentUserEmail);
        staffUser.setEnabled(enabled);
        return mapUserToDto(userRepository.save(staffUser));
    }

    @Override
    public void resetStaffPassword(Long userId, String currentUserEmail) {
        User staffUser = getManagedStaffUser(userId, currentUserEmail);
        String temporaryPassword = generateTemporaryPassword();
        staffUser.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(staffUser);

        emailService.sendEmail(
                staffUser.getEmail(),
                "Tapro password reset",
                "Hello " + staffUser.getName() + ",\n\n" +
                        "Your Tapro password has been reset by the restaurant owner.\n\n" +
                        "Temporary password: " + temporaryPassword + "\n\n" +
                        "Please sign in and change your password immediately.\n\n" +
                        "Tapro Team"
        );
    }

    @Override
    public OwnerAnalyticsResponse getAnalytics(String currentUserEmail) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);
        List<Order> orders = orderRepository.findByTenantId(restaurant.getId());
        List<Order> activeOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        double revenueToday = sumRevenueSince(activeOrders, today.atStartOfDay());
        double revenueWeek = sumRevenueSince(activeOrders, weekStart.atStartOfDay());
        double revenueMonth = sumRevenueSince(activeOrders, monthStart.atStartOfDay());

        long ordersToday = countOrdersSince(activeOrders, today.atStartOfDay());
        long ordersWeek = countOrdersSince(activeOrders, weekStart.atStartOfDay());
        long ordersMonth = countOrdersSince(activeOrders, monthStart.atStartOfDay());

        Map<Long, ItemSalesAccumulator> itemSales = new LinkedHashMap<>();
        Map<Integer, Long> hourlyCounts = new LinkedHashMap<>();

        for (Order order : activeOrders) {
            hourlyCounts.merge(order.getCreatedAt().getHour(), 1L, Long::sum);

            if (order.getItems() == null) {
                continue;
            }

            order.getItems().forEach(item -> {
                ItemSalesAccumulator accumulator = itemSales.computeIfAbsent(
                        item.getMenuItemId(),
                        ignored -> new ItemSalesAccumulator(item.getMenuItemId(), item.getItemName())
                );
                accumulator.quantity += item.getQuantity();
                accumulator.revenue += item.getSubTotal() != null ? item.getSubTotal() : 0D;
            });
        }

        List<OwnerItemSalesResponse> rankedItems = itemSales.values().stream()
                .map(value -> new OwnerItemSalesResponse(value.menuItemId, value.itemName, value.quantity, value.revenue))
                .collect(Collectors.toList());

        List<OwnerItemSalesResponse> topSellingItems = rankedItems.stream()
                .sorted(Comparator.comparingLong(OwnerItemSalesResponse::getQuantity).reversed()
                        .thenComparingDouble(OwnerItemSalesResponse::getRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<OwnerItemSalesResponse> leastSellingItems = rankedItems.stream()
                .sorted(Comparator.comparingLong(OwnerItemSalesResponse::getQuantity)
                        .thenComparingDouble(OwnerItemSalesResponse::getRevenue))
                .limit(5)
                .collect(Collectors.toList());

        List<OwnerPeakHourResponse> peakOrderingHours = hourlyCounts.entrySet().stream()
                .map(entry -> new OwnerPeakHourResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(OwnerPeakHourResponse::getOrderCount).reversed()
                        .thenComparingInt(OwnerPeakHourResponse::getHour))
                .limit(5)
                .collect(Collectors.toList());

        double averageOrderValue = activeOrders.isEmpty()
                ? 0D
                : activeOrders.stream().mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0D).sum() / activeOrders.size();

        return new OwnerAnalyticsResponse(
                new OwnerMetricPeriodValue(revenueToday, revenueWeek, revenueMonth),
                new OwnerMetricPeriodValue(ordersToday, ordersWeek, ordersMonth),
                topSellingItems,
                leastSellingItems,
                peakOrderingHours,
                averageOrderValue
        );
    }

    // =========================
    // SAFE MAPPING (FIXED)
    // =========================
    private OrderResponseDTO mapToDTO(Order order) {
        String restaurantCurrencyCode = order.getTenantId() != null
                ? restaurantRepository.findById(order.getTenantId())
                        .map(restaurant -> RestaurantCurrencySupport.toApiValue(restaurant.getCurrencyCode()))
                        .orElse(RestaurantCurrencySupport.toApiValue(null))
                : RestaurantCurrencySupport.toApiValue(null);

        return new OrderResponseDTO(
                order.getId(),
                order.getTenantId(),
                order.getTenantId(),
                order.getTableNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                restaurantCurrencyCode,
                order.getCreatedAt(),
                order.getItems() != null
                        ? order.getItems().stream()
                                .map(item -> new OrderItemResponseDTO(
                                        item.getId(),
                                        item.getMenuItemId(),
                                        item.getItemName(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getSubTotal()
                                ))
                                .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }

    private OwnerStaffUserResponse mapUserToDto(User user) {
        return new OwnerStaffUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getRestaurant() != null ? user.getRestaurant().getId() : null,
                user.getRestaurant() != null ? user.getRestaurant().getName() : null
        );
    }

    private Restaurant getOwnerRestaurant(String currentUserEmail) {
        User owner = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        restaurantAccessGuard.ensureRestaurantAccessAllowed(owner);

        if (owner.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only owners can manage staff");
        }

        if (owner.getRestaurant() == null) {
            throw new RuntimeException("Owner is not assigned to a restaurant");
        }

        return owner.getRestaurant();
    }

    private void authorizeOrderAccess(Long restaurantId, String currentUserEmail) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);
        if (!restaurant.getId().equals(restaurantId)) {
            throw new AccessDeniedException("Owners can view orders only for their assigned restaurant");
        }
    }

    private void validatePastOrderStatuses(List<OrderStatus> statuses) {
        boolean valid = statuses.stream().allMatch(PAST_ORDER_STATUSES::contains);
        if (!valid) {
            throw new AccessDeniedException("Past orders support only COMPLETED or CANCELLED status filters");
        }
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<Order> filterByTableNumber(List<Order> orders, String tableNumber) {
        String normalized = normalizeFilter(tableNumber);
        if (normalized == null) {
            return orders;
        }

        return orders.stream()
                .filter(order -> order.getTableNumber() != null
                        && order.getTableNumber().trim().equalsIgnoreCase(normalized))
                .collect(Collectors.toList());
    }

    private User getManagedStaffUser(Long userId, String currentUserEmail) {
        Restaurant restaurant = getOwnerRestaurant(currentUserEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRestaurant() == null || !restaurant.getId().equals(user.getRestaurant().getId())) {
            throw new AccessDeniedException("Owners can manage only staff assigned to their restaurant");
        }

        if (!List.of(Role.STAFF, Role.KITCHEN).contains(user.getRole())) {
            throw new AccessDeniedException("Owners can manage only STAFF and KITCHEN users");
        }

        return user;
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();
        for (int index = 0; index < 10; index++) {
            password.append(RESET_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(RESET_PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    private double sumRevenueSince(List<Order> orders, LocalDateTime start) {
        return orders.stream()
                .filter(order -> !order.getCreatedAt().isBefore(start))
                .mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0D)
                .sum();
    }

    private long countOrdersSince(List<Order> orders, LocalDateTime start) {
        return orders.stream()
                .filter(order -> !order.getCreatedAt().isBefore(start))
                .count();
    }

    private static class ItemSalesAccumulator {
        private final Long menuItemId;
        private final String itemName;
        private long quantity;
        private double revenue;

        private ItemSalesAccumulator(Long menuItemId, String itemName) {
            this.menuItemId = menuItemId;
            this.itemName = itemName;
        }
    }
}
