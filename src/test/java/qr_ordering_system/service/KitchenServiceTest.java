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
import qr_ordering_system.model.CurrencyCode;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OrderStatusTransitionValidator;
import qr_ordering_system.service.impl.KitchenServiceImpl;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

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
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant(1L)));

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
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant(1L)));

        var result = kitchenService.updateOrderStatus(1L, OrderStatus.PREPARING);

        assertNotNull(result);
        assertEquals("PREPARING", result.getStatus());
    }

    private Restaurant restaurant(Long tenantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(tenantId);
        restaurant.setCurrencyCode(CurrencyCode.LKR);
        return restaurant;
    }
}
