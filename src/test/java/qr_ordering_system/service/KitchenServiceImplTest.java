package qr_ordering_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import qr_ordering_system.config.TenantContext;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OrderStatusTransitionValidator;
import qr_ordering_system.service.impl.KitchenServiceImpl;

@ExtendWith(MockitoExtension.class)
class KitchenServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderStatusTransitionValidator orderStatusTransitionValidator;

    @InjectMocks
    private KitchenServiceImpl kitchenService;

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void getKitchenOrders_shouldReturnList() {

        Order order = new Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findByTenantIdAndStatus(1L, OrderStatus.PENDING))
                .thenReturn(List.of(order));

        var result = kitchenService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void updateStatus_shouldUpdateSuccessfully() {

        Order order = new Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        var result = kitchenService.updateOrderStatus(1L, OrderStatus.PREPARING);

        assertNotNull(result);
        assertEquals("PREPARING", result.getStatus());
    }

    @Test
    void getKitchenOrders_shouldReturnOnlyActiveStatusesForOwnRestaurant() {
        Order order = new Order();
        order.setId(2L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of(order));

        var result = kitchenService.getKitchenOrders();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        doThrow(new BadRequestException("Invalid status transition: PENDING -> READY"))
                .when(orderStatusTransitionValidator)
                .validateKitchenOrOwnerTransition(OrderStatus.PENDING, OrderStatus.READY);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> kitchenService.updateOrderStatus(1L, OrderStatus.READY)
        );

        assertEquals("Invalid status transition: PENDING -> READY", exception.getMessage());
    }

    @Test
    void updateStatus_shouldAllowReadyToCompleted() {
        Order order = new Order();
        order.setId(5L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.READY);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        var result = kitchenService.updateOrderStatus(5L, OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED.name(), result.getStatus());
    }
}
