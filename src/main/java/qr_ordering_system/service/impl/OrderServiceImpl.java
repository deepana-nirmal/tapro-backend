package qr_ordering_system.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.config.TenantContext;
import qr_ordering_system.dto.OrderItemDTO;
import qr_ordering_system.dto.OrderItemResponseDTO;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderItem;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.EmailService;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OrderService;
import qr_ordering_system.service.RestaurantCurrencySupport;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final RestaurantAccessGuard restaurantAccessGuard;

    // =========================
    // CREATE ORDER
    // =========================
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {

        Long tenantId = dto.getRestaurantId();
        restaurantAccessGuard.requireActiveRestaurant(tenantId);

        log.info("Creating order for restaurantId={} tableNumber={}", tenantId, dto.getTableNumber());

        Order order = new Order();
        order.setTenantId(tenantId);
        order.setTableNumber(dto.getTableNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (OrderItemDTO itemDTO : dto.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Menu item not found: " + itemDTO.getMenuItemId()));

            if (menuItem.getRestaurant() == null || !tenantId.equals(menuItem.getRestaurant().getId())) {
                throw new BadRequestException("Menu item does not belong to the selected restaurant");
            }

            if (menuItem.getStatus() != MenuItemStatus.AVAILABLE) {
                throw new BadRequestException("Menu item not available: " + menuItem.getName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItemId(menuItem.getId());
            item.setItemName(menuItem.getName());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(menuItem.getPrice());
            item.setSubTotal(menuItem.getPrice() * itemDTO.getQuantity());

            total += item.getSubTotal();
            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        OrderResponseDTO response = mapToDTO(saved);

        messagingTemplate.convertAndSend("/topic/orders/" + tenantId, response);

        String msg = "New order placed at table " + saved.getTableNumber();
        notificationService.create(msg, "ORDER");
        messagingTemplate.convertAndSend("/topic/notifications/" + tenantId, msg);

        log.info(
                "Created order id={} restaurantId={} tableNumber={} status={}",
                saved.getId(),
                saved.getTenantId(),
                saved.getTableNumber(),
                saved.getStatus()
        );

        return response;
    }

    @Override
    public OrderResponseDTO getPublicOrderById(Long restaurantId, Long orderId) {
        log.info("Public tracking request orderId={} restaurantId={}", orderId, restaurantId);
        restaurantAccessGuard.requireActiveRestaurant(restaurantId);

        Order orderById = orderRepository.findById(orderId).orElse(null);
        Order orderByIdAndRestaurant = orderRepository.findByIdAndTenantId(orderId, restaurantId).orElse(null);

        log.info(
                "Public tracking lookup orderId={} existsByIdOnly={} existsByIdAndRestaurant={} resolvedRestaurantId={}",
                orderId,
                orderById != null,
                orderByIdAndRestaurant != null,
                orderById != null ? orderById.getTenantId() : null
        );

        Order order = orderByIdAndRestaurant;
        if (order == null) {
            throw new ResourceNotFoundException("Order not found");
        }

        return mapToDTO(order);
    }

    @Override
    public OrderResponseDTO getPublicOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        restaurantAccessGuard.requireActiveRestaurant(order.getTenantId());
        return mapToDTO(order);
    }

    // =========================
    // GET BY ID
    // =========================
    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        return mapToDTO(getOrderIfValid(orderId));
    }

    // =========================
    // GET BY STATUS
    // =========================
    @Override
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {

        return orderRepository.findByTenantIdAndStatus(
                        TenantContext.getTenantId(), status)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================
    // GET BY TABLE
    // =========================
    @Override
    public List<OrderResponseDTO> getOrdersByTable(String tableNumber) {

        return orderRepository.findByTenantIdAndTableNumber(
                        TenantContext.getTenantId(), tableNumber)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================
    // GET ALL
    // =========================
    @Override
    public List<OrderResponseDTO> getAllOrders() {

        return orderRepository.findByTenantId(TenantContext.getTenantId())
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================
    // UPDATE STATUS
    // =========================
    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = getOrderIfValid(orderId);

        order.setStatus(newStatus);

        Order updated = orderRepository.save(order);

        OrderResponseDTO dto = mapToDTO(updated);

        String msg = "Order #" + orderId + " is now " + newStatus;

        messagingTemplate.convertAndSend("/topic/orders/" + order.getTenantId(), dto);
        messagingTemplate.convertAndSend("/topic/notifications/" + order.getTenantId(), msg);

        notificationService.create(msg, "ORDER");

        return dto;
    }

    // =========================
    // CANCEL
    // =========================
    @Override
    public void cancelOrder(Long orderId) {

        Order order = getOrderIfValid(orderId);

        if (order.getStatus() == OrderStatus.COMPLETED ||
            order.getStatus() == OrderStatus.READY) {
            throw new BadRequestException("Cannot cancel completed/ready order");
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        String msg = "Order " + orderId + " cancelled";

        messagingTemplate.convertAndSend("/topic/orders/" + order.getTenantId(), msg);
        messagingTemplate.convertAndSend("/topic/notifications/" + order.getTenantId(), msg);

        notificationService.create(msg, "ORDER");
    }

    // =========================
    // DELETE
    // =========================
    @Override
    public void deleteOrder(Long orderId) {

        Order order = getOrderIfValid(orderId);

        orderRepository.delete(order);

        String msg = "Order " + orderId + " deleted";

        messagingTemplate.convertAndSend("/topic/orders/" + order.getTenantId(), msg);
        messagingTemplate.convertAndSend("/topic/notifications/" + order.getTenantId(), msg);

        notificationService.create(msg, "SYSTEM");
    }

    // =========================
    // SAFETY CHECK
    // =========================
    private Order getOrderIfValid(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getTenantId().equals(TenantContext.getTenantId())) {
            throw new BadRequestException("Access denied (tenant mismatch)");
        }

        return order;
    }

    // =========================
    // MAPPER
    // =========================
    private OrderResponseDTO mapToDTO(Order order) {
        String restaurantCurrencyCode = order.getTenantId() != null
                ? restaurantRepository.findById(order.getTenantId())
                        .map(restaurant -> RestaurantCurrencySupport.toApiValue(restaurant.getCurrencyCode()))
                        .orElse(RestaurantCurrencySupport.toApiValue(null))
                : RestaurantCurrencySupport.toApiValue(null);

        List<OrderItemResponseDTO> items = order.getItems() != null
                ? order.getItems().stream()
                .map(i -> new OrderItemResponseDTO(
                        i.getId(),
                        i.getMenuItemId(),
                        i.getItemName(),
                        i.getQuantity(),
                        i.getPrice(),
                        i.getSubTotal()
                ))
                .collect(Collectors.toList())
                : List.of();

        return new OrderResponseDTO(
                order.getId(),
                order.getTenantId(),
                order.getTenantId(),
                order.getTableNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                restaurantCurrencyCode,
                order.getCreatedAt(),
                items
        );
    }
}
