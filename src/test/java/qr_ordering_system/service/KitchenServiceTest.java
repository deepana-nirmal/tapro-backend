package qr_ordering_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import qr_ordering_system.config.TenantContext;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.impl.KitchenServiceImpl;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;

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
    void testGetKitchenOrders() {

        Order order = new Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findByTenantIdAndStatus(
                anyLong(),
                any(OrderStatus.class)
        )).thenReturn(List.of(order));

        var result = kitchenService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testUpdateStatus_success() {

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
}
