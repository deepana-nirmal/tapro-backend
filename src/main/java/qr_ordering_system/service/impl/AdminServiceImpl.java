package qr_ordering_system.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerItemSalesResponse;
import qr_ordering_system.dto.OwnerMetricPeriodValue;
import qr_ordering_system.dto.OwnerPeakHourResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.dto.OrderItemResponseDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.dto.SuperAdminUserRequestDTO;
import qr_ordering_system.dto.SuperAdminUserResponseDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.AdminService;
import qr_ordering_system.service.CategoryService;
import qr_ordering_system.service.MenuItemService;
import qr_ordering_system.service.TableService;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final EnumSet<Role> MANAGEABLE_ROLES =
            EnumSet.of(Role.OWNER, Role.STAFF, Role.KITCHEN);
    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.PREPARING,
            OrderStatus.READY
    );
    private static final List<OrderStatus> PAST_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
    );

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final MenuItemService menuItemService;
    private final CategoryService categoryService;
    private final TableService tableService;

    public AdminServiceImpl(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            MenuItemService menuItemService,
            CategoryService categoryService,
            TableService tableService) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
        this.tableService = tableService;
    }

    @Override
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    public void suspendRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.SUSPENDED);
        restaurantRepository.save(restaurant);
    }

    @Override
    public void activateRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuperAdminUserResponseDTO> getAllUsers() {
        return userRepository.findAllWithRestaurant().stream()
                .sorted(userComparator())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsersByRestaurantResponseDTO> getUsersByRestaurant() {
        Map<Long, UsersByRestaurantResponseDTO> grouped = new LinkedHashMap<>();

        for (Restaurant restaurant : restaurantRepository.findAllByOrderByNameAsc()) {
            grouped.put(
                    restaurant.getId(),
                    new UsersByRestaurantResponseDTO(restaurant.getId(), restaurant.getName())
            );
        }

        UsersByRestaurantResponseDTO platformGroup =
                new UsersByRestaurantResponseDTO(null, "Unassigned / Platform Users");

        for (User user : userRepository.findAllWithRestaurant().stream().sorted(userComparator()).toList()) {
            SuperAdminUserResponseDTO response = toResponse(user);

            if (user.getRole() == Role.SUPER_ADMIN) {
                platformGroup.getSuperAdmins().add(response);
                continue;
            }

            if (user.getRestaurant() == null) {
                platformGroup.getUnassigned().add(response);
                continue;
            }

            UsersByRestaurantResponseDTO restaurantGroup = grouped.computeIfAbsent(
                    user.getRestaurant().getId(),
                    id -> new UsersByRestaurantResponseDTO(id, user.getRestaurant().getName())
            );

            if (user.getRole() == Role.OWNER) {
                restaurantGroup.getOwners().add(response);
            } else if (user.getRole() == Role.KITCHEN) {
                restaurantGroup.getKitchen().add(response);
            } else {
                restaurantGroup.getStaff().add(response);
            }
        }

        List<UsersByRestaurantResponseDTO> response = new ArrayList<>(grouped.values());
        response.add(platformGroup);
        return response;
    }

    @Override
    public SuperAdminUserResponseDTO createUser(SuperAdminUserRequestDTO request) {
        validateManagedRole(request.getRole());

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required when creating a user");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Restaurant restaurant = resolveRestaurantForRole(request.getRole(), request.getRestaurantId());

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRestaurant(restaurant);
        user.setEnabled(request.getEnabled() == null || request.getEnabled());

        return toResponse(userRepository.save(user));
    }

    @Override
    public SuperAdminUserResponseDTO updateUser(Long id, SuperAdminUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateManagedTarget(user);
        validateManagedRole(request.getRole());

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BadRequestException("Email already exists");
        }

        Restaurant restaurant = resolveRestaurantForRole(request.getRole(), request.getRestaurantId());

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim());
        user.setRole(request.getRole());
        user.setRestaurant(restaurant);
        user.setEnabled(request.getEnabled() == null || request.getEnabled());

        return toResponse(userRepository.save(user));
    }

    @Override
    public SuperAdminUserResponseDTO enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateManagedTarget(user);
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    @Override
    public SuperAdminUserResponseDTO disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateManagedTarget(user);
        user.setEnabled(false);
        return toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateManagedTarget(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getRestaurantActiveOrders(Long restaurantId) {
        validateRestaurant(restaurantId);
        return orderRepository.findOrdersForRestaurant(restaurantId, ACTIVE_ORDER_STATUSES, null, null)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getRestaurantPastOrders(
            Long restaurantId,
            LocalDate from,
            LocalDate to,
            OrderStatus status,
            String tableNumber) {
        validateRestaurant(restaurantId);
        List<OrderStatus> statuses = status != null ? List.of(status) : PAST_ORDER_STATUSES;
        if (!statuses.stream().allMatch(PAST_ORDER_STATUSES::contains)) {
            throw new BadRequestException("Past orders support only COMPLETED or CANCELLED status filters");
        }

        return filterByTableNumber(orderRepository.findOrdersForRestaurant(
                        restaurantId,
                        statuses,
                        from != null ? from.atStartOfDay() : null,
                        to != null ? to.plusDays(1).atStartOfDay() : null
                ), tableNumber)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerStaffUserResponse> getRestaurantUsers(Long restaurantId) {
        Restaurant restaurant = validateRestaurant(restaurantId);
        return userRepository.findByRestaurant_IdAndRoleIn(restaurant.getId(), List.of(Role.STAFF, Role.KITCHEN))
                .stream()
                .map(user -> new OwnerStaffUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isEnabled(),
                        restaurant.getId(),
                        restaurant.getName()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerAnalyticsResponse getRestaurantAnalytics(Long restaurantId) {
        Restaurant restaurant = validateRestaurant(restaurantId);
        List<Order> orders = orderRepository.findByTenantId(restaurant.getId());
        List<Order> revenueOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.READY)
                .toList();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        double revenueToday = sumRevenueSince(revenueOrders, today.atStartOfDay());
        double revenueWeek = sumRevenueSince(revenueOrders, weekStart.atStartOfDay());
        double revenueMonth = sumRevenueSince(revenueOrders, monthStart.atStartOfDay());

        long ordersToday = countOrdersSince(orders, today.atStartOfDay());
        long ordersWeek = countOrdersSince(orders, weekStart.atStartOfDay());
        long ordersMonth = countOrdersSince(orders, monthStart.atStartOfDay());

        Map<Long, ItemSalesAccumulator> itemSales = new LinkedHashMap<>();
        Map<Integer, Long> hourlyCounts = new LinkedHashMap<>();

        for (Order order : orders) {
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
                .toList();

        List<OwnerItemSalesResponse> leastSellingItems = rankedItems.stream()
                .sorted(Comparator.comparingLong(OwnerItemSalesResponse::getQuantity)
                        .thenComparingDouble(OwnerItemSalesResponse::getRevenue))
                .limit(5)
                .toList();

        List<OwnerPeakHourResponse> peakOrderingHours = hourlyCounts.entrySet().stream()
                .map(entry -> new OwnerPeakHourResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(OwnerPeakHourResponse::getOrderCount).reversed()
                        .thenComparingInt(OwnerPeakHourResponse::getHour))
                .limit(5)
                .toList();

        double averageOrderValue = revenueOrders.isEmpty()
                ? 0D
                : revenueOrders.stream().mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0D).sum() / revenueOrders.size();

        return new OwnerAnalyticsResponse(
                new OwnerMetricPeriodValue(revenueToday, revenueWeek, revenueMonth),
                new OwnerMetricPeriodValue(ordersToday, ordersWeek, ordersMonth),
                topSellingItems,
                leastSellingItems,
                peakOrderingHours,
                averageOrderValue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getRestaurantMenuItems(Long restaurantId, String currentUserEmail) {
        validateRestaurant(restaurantId);
        return menuItemService.getRestaurantMenuForManagement(restaurantId, currentUserEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getRestaurantCategories(Long restaurantId, String currentUserEmail) {
        validateRestaurant(restaurantId);
        return categoryService.getCategoriesByRestaurant(restaurantId, currentUserEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableResponseDTO> getRestaurantTables(Long restaurantId, String currentUserEmail) {
        validateRestaurant(restaurantId);
        return tableService.getRestaurantTables(restaurantId, currentUserEmail);
    }

    private void validateManagedRole(Role role) {
        if (!MANAGEABLE_ROLES.contains(role)) {
            throw new BadRequestException("Only OWNER, STAFF, or KITCHEN users can be managed here");
        }
    }

    private void validateManagedTarget(User user) {
        if (!MANAGEABLE_ROLES.contains(user.getRole())) {
            throw new BadRequestException("This user cannot be managed from the super admin users screen");
        }
    }

    private Restaurant resolveRestaurantForRole(Role role, Long restaurantId) {
        if (MANAGEABLE_ROLES.contains(role)) {
            if (restaurantId == null) {
                throw new BadRequestException("Restaurant is required for OWNER, STAFF, and KITCHEN users");
            }

            return restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        }

        return null;
    }

    private Restaurant validateRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private OrderResponseDTO toOrderResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getTenantId(),
                order.getTenantId(),
                order.getTableNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
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
                        .toList()
                        : List.of()
        );
    }

    private List<Order> filterByTableNumber(List<Order> orders, String tableNumber) {
        if (tableNumber == null || tableNumber.isBlank()) {
            return orders;
        }

        String normalized = tableNumber.trim();
        return orders.stream()
                .filter(order -> order.getTableNumber() != null
                        && order.getTableNumber().trim().equalsIgnoreCase(normalized))
                .toList();
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

    private SuperAdminUserResponseDTO toResponse(User user) {
        return new SuperAdminUserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRestaurant() != null ? user.getRestaurant().getId() : null,
                user.getRestaurant() != null ? user.getRestaurant().getName() : null,
                user.isEnabled()
        );
    }

    private Comparator<User> userComparator() {
        return Comparator
                .comparing((User user) -> user.getRestaurant() != null ? user.getRestaurant().getName() : "ZZZ")
                .thenComparing(user -> user.getRole().name())
                .thenComparing(user -> user.getName() != null ? user.getName() : "")
                .thenComparing(User::getEmail);
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
