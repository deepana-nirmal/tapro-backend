package qr_ordering_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import qr_ordering_system.config.TenantContext;
import qr_ordering_system.dto.OrderItemDTO;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.MenuItem;
import qr_ordering_system.model.MenuItemStatus;
import qr_ordering_system.model.Order;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.CurrencyCode;
import qr_ordering_system.repository.MenuItemRepository;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;
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
    void createOrder_shouldReturnResponse() {

        MenuItem menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Rice");
        menuItem.setPrice(100.0);
        menuItem.setStatus(MenuItemStatus.AVAILABLE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setCurrencyCode(CurrencyCode.LKR);
        menuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(1L))
                .thenReturn(Optional.of(menuItem));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(1L);
        request.setTableNumber("T1");

        OrderItemDTO item = new OrderItemDTO();
        item.setMenuItemId(1L);
        item.setQuantity(2);

        request.setItems(List.of(item));

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getTenantId());
        assertEquals(1L, response.getRestaurantId());
        assertEquals(200.0, response.getTotalAmount());
    }

    @Test
    void createOrder_shouldRejectSuspendedRestaurant() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(99L);
        request.setTableNumber("T1");
        request.setItems(List.of(orderItem(1L, 1)));

        when(restaurantAccessGuard.requireActiveRestaurant(99L))
                .thenThrow(new BadRequestException("Restaurant is unavailable"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals("Restaurant is unavailable", exception.getMessage());
    }

    @Test
    void createOrder_shouldPersistRestaurantAndItemsForActiveRestaurant() {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Rice");
        menuItem.setPrice(100.0);
        menuItem.setStatus(MenuItemStatus.AVAILABLE);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(5L);
        restaurant.setCurrencyCode(CurrencyCode.USD);
        menuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(restaurant));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setRestaurantId(5L);
        request.setTableNumber("A3");
        request.setItems(List.of(orderItem(1L, 3)));

        orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        org.mockito.Mockito.verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(5L, savedOrder.getTenantId());
        assertEquals("A3", savedOrder.getTableNumber());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(300.0, savedOrder.getTotalAmount());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals("Rice", savedOrder.getItems().get(0).getItemName());
    }

    @Test
    void getOrderById_shouldReturnOrder() {

        Order order = new Order();
        order.setId(1L);
        order.setTenantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant(1L, CurrencyCode.LKR)));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getPublicOrderById_shouldReturnOrderForMatchingRestaurant() {
        Order order = new Order();
        order.setId(4L);
        order.setTenantId(1L);
        order.setTableNumber("A1");
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(orderRepository.findByIdAndTenantId(4L, 1L)).thenReturn(Optional.of(order));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant(1L, CurrencyCode.LKR)));

        OrderResponseDTO response = orderService.getPublicOrderById(1L, 4L);

        assertEquals(4L, response.getId());
        assertEquals(1L, response.getRestaurantId());
        verify(orderRepository).findByIdAndTenantId(4L, 1L);
    }

    @Test
    void getPublicOrderById_shouldRejectWrongRestaurantId() {
        Order order = new Order();
        order.setId(4L);
        order.setTenantId(2L);
        order.setTableNumber("B1");
        order.setStatus(OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(orderRepository.findByIdAndTenantId(4L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getPublicOrderById(1L, 4L));
        verify(orderRepository).findByIdAndTenantId(eq(4L), eq(1L));
    }

    private Restaurant restaurant(Long id, CurrencyCode currencyCode) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setCurrencyCode(currencyCode);
        return restaurant;
    }

    private OrderItemDTO orderItem(Long menuItemId, int quantity) {
        OrderItemDTO item = new OrderItemDTO();
        item.setMenuItemId(menuItemId);
        item.setQuantity(quantity);
        return item;
    }
}
