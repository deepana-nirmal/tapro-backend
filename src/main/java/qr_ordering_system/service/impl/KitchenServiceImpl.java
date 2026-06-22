package qr_ordering_system.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.config.TenantContext;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.dto.OrderItemResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.service.KitchenService;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OrderStatusTransitionValidator;
import qr_ordering_system.service.RestaurantCurrencySupport;

import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.PREPARING,
            OrderStatus.READY
    );

    private static final List<OrderStatus> PAST_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
    );

    private static final Logger log = LoggerFactory.getLogger(KitchenServiceImpl.class);

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final OrderStatusTransitionValidator orderStatusTransitionValidator;

    // =========================
    // KITCHEN ORDERS (SAAS SAFE)
    // =========================
    @Override
    public List<OrderResponseDTO> getKitchenOrders() {

        Long tenantId = TenantContext.getTenantId();

        return orderRepository
                .findByTenantIdAndStatusIn(tenantId, ACTIVE_ORDER_STATUSES)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getPastOrders() {

        Long tenantId = TenantContext.getTenantId();

        return orderRepository
                .findByTenantIdAndStatusIn(tenantId, PAST_ORDER_STATUSES)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER BY STATUS
    // =========================
    @Override
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {

        Long tenantId = TenantContext.getTenantId();

        return orderRepository
                .findByTenantIdAndStatus(tenantId, status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================
    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status) {

        log.info("Updating order {} to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // 🔐 Tenant check
        if (!order.getTenantId().equals(TenantContext.getTenantId())) {
            throw new BadRequestException("Access denied (tenant mismatch)");
        }

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

    // =========================
    // BUSINESS RULES
    // =========================
    // =========================
    // DTO MAPPER
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
