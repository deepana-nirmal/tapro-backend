package qr_ordering_system.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import qr_ordering_system.config.TenantContext;
import qr_ordering_system.dto.OrderItemDTO;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private RestaurantAccessGuard restaurantAccessGuard;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void createOrder_shouldSaveAndReturnResponse() {

        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Rice");
        item.setPrice(100.0);
        item.setStatus(MenuItemStatus.AVAILABLE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        item.setRestaurant(restaurant);

        when(menuItemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(orderRepository.save(any(qr_ordering_system.model.Order.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderItemDTO dto = new OrderItemDTO();
        dto.setMenuItemId(1L);
        dto.setQuantity(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("T1");
        request.setItems(List.of(dto));

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getTenantId());
    }

    @Test
    void updateOrderStatus_shouldWork() {

        qr_ordering_system.model.Order order =
                new qr_ordering_system.model.Order();

        order.setId(1L);
        order.setTenantId(1L);
        order.setTableNumber("T1");
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(qr_ordering_system.model.Order.class)))
                .thenReturn(order);

        OrderResponseDTO response =
                orderService.updateOrderStatus(1L, OrderStatus.READY);

        assertNotNull(response);
    }

    @Test
    void cancelOrder_shouldWork() {

        qr_ordering_system.model.Order order =
                new qr_ordering_system.model.Order();

        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        verify(orderRepository).save(any());
    }

    @Test
    void deleteOrder_shouldWork() {

        qr_ordering_system.model.Order order =
                new qr_ordering_system.model.Order();

        order.setId(1L);
        order.setTenantId(1L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(any());
    }
}
