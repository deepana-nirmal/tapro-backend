package qr_ordering_system.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import qr_ordering_system.config.TenantContext;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.EmailService;
import qr_ordering_system.service.NotificationService;
import qr_ordering_system.service.OrderStatusTransitionValidator;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.impl.OwnerServiceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class OwnerServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RestaurantAccessGuard restaurantAccessGuard;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderStatusTransitionValidator orderStatusTransitionValidator;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    private qr_ordering_system.model.Order order;

    @BeforeEach
    void setup() {
        TenantContext.setTenantId(1L);

        order = new qr_ordering_system.model.Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setTableNumber("T1");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(100.0);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void testGetAllOrders() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(1L)));

        when(orderRepository.findByTenantId(1L))
                .thenReturn(List.of(order));

        List<?> result = ownerService.getAllOrders(1L, "owner@demo.com");

        assertEquals(1, result.size());
    }

    @Test
    void testGetOrdersByStatus() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(1L)));

        when(orderRepository.findByTenantIdAndStatus(1L, OrderStatus.PENDING))
                .thenReturn(List.of(order));

        List<?> result =
                ownerService.getOrdersByStatus(1L, OrderStatus.PENDING, "owner@demo.com");

        assertEquals(1, result.size());
    }

    @Test
    void testGetAllOrders_ShouldRejectOtherRestaurant() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(2L)));

        assertThrows(RuntimeException.class, () -> ownerService.getAllOrders(1L, "owner@demo.com"));
    }

    @Test
    void testGetOrderById() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var result = ownerService.getOrderById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetOrderById_NotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ownerService.getOrderById(1L);
        });
    }

    @Test
    void getActiveOrders_shouldRequestOnlyActiveStatusesForOwnersRestaurant() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(7L)));

        Order pendingOrder = new Order();
        pendingOrder.setId(21L);
        pendingOrder.setTenantId(7L);
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setTableNumber("A1");
        pendingOrder.setTotalAmount(250.0);

        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(List.of(pendingOrder));

        var result = ownerService.getActiveOrders("owner@demo.com");

        ArgumentCaptor<List<OrderStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(7L),
                statusesCaptor.capture(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()
        );

        assertEquals(List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY), statusesCaptor.getValue());
        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getRestaurantId());
    }

    @Test
    void getActiveOrders_shouldExcludeCompletedOrders() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(7L)));

        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY)),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(List.of());

        var result = ownerService.getActiveOrders("owner@demo.com");

        assertEquals(0, result.size());
    }

    @Test
    void getPastOrders_shouldRequestCompletedAndCancelledForOwnersRestaurant() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(3L)));

        Order completedOrder = new Order();
        completedOrder.setId(31L);
        completedOrder.setTenantId(3L);
        completedOrder.setStatus(OrderStatus.COMPLETED);
        completedOrder.setTableNumber("T9");
        completedOrder.setTotalAmount(300.0);

        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("T9")
        )).thenReturn(List.of(completedOrder));

        var result = ownerService.getPastOrders("owner@demo.com", null, null, null, "T9");

        ArgumentCaptor<List<OrderStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(3L),
                statusesCaptor.capture(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("T9")
        );

        assertEquals(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED), statusesCaptor.getValue());
        assertEquals(1, result.size());
        assertEquals(OrderStatus.COMPLETED.name(), result.get(0).getStatus());
    }

    @Test
    void getAllOrders_shouldOnlyAllowOwnersOwnRestaurant() {
        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(ownerUser(2L)));

        assertThrows(AccessDeniedException.class, () -> ownerService.getAllOrders(1L, "owner@demo.com"));
    }

    private User ownerUser(Long restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);

        User user = new User();
        user.setEmail("owner@demo.com");
        user.setRole(Role.OWNER);
        user.setRestaurant(restaurant);
        return user;
    }
}
