package qr_ordering_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import qr_ordering_system.dto.RestaurantRequestDTO;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.model.CurrencyCode;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.impl.RestaurantServiceImpl;

class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private Restaurant restaurant;
    private RestaurantRequestDTO request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Harbor Table");
        restaurant.setAddress("120 Ocean Ave");
        restaurant.setPhone("+1 555 410 9001");
        restaurant.setEmail("hello@harbortable.com");
        restaurant.setCurrencyCode(CurrencyCode.LKR);
        restaurant.setStatus(RestaurantStatus.ACTIVE);

        request = new RestaurantRequestDTO();
        request.setName("Harbor Table");
        request.setAddress("120 Ocean Ave");
        request.setPhone("+1 555 410 9001");
        request.setEmail("hello@harbortable.com");
        request.setLogoUrl("https://cdn.example.com/logo.png");
        request.setDescription("Coastal dining");
        request.setOpeningHours("Mon-Sun");
        request.setServiceChargePercentage(10.0);
        request.setTaxPercentage(8.0);
        request.setCurrencyCode("USD");
        request.setThemeColor("#10b981");
    }

    @Test
    void testCreateRestaurant() {
        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of());
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        ))
                .thenReturn(List.of());

        when(restaurantRepository.save(org.mockito.ArgumentMatchers.any(Restaurant.class)))
                .thenAnswer(invocation -> {
                    Restaurant saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        RestaurantResponseDTO response = restaurantService.createRestaurant(request);

        assertEquals("Harbor Table", response.getName());
        assertEquals(RestaurantStatus.ACTIVE, response.getStatus());
        assertEquals("USD", response.getCurrencyCode());
        verify(restaurantRepository, times(1)).save(org.mockito.ArgumentMatchers.any(Restaurant.class));
    }

    @Test
    void testCreateRestaurantDefaultsCurrencyToLkr() {
        request.setCurrencyCode(null);

        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of());
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(List.of());
        when(restaurantRepository.save(org.mockito.ArgumentMatchers.any(Restaurant.class)))
                .thenAnswer(invocation -> {
                    Restaurant saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        RestaurantResponseDTO response = restaurantService.createRestaurant(request);

        assertEquals("LKR", response.getCurrencyCode());
    }

    @Test
    void testCreateRestaurantRejectsInvalidCurrency() {
        request.setCurrencyCode("EUR");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> restaurantService.createRestaurant(request));

        assertEquals("Invalid currency code. Supported currencies: LKR, USD", exception.getMessage());
    }

    @Test
    void testListRestaurants() {
        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of());
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        ))
                .thenReturn(List.of());
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<RestaurantResponseDTO> restaurants = restaurantService.getAllRestaurants();

        assertEquals(1, restaurants.size());
        assertEquals("Harbor Table", restaurants.get(0).getName());
    }

    @Test
    void testActivateRestaurant() {
        restaurant.setStatus(RestaurantStatus.SUSPENDED);
        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of());
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        ))
                .thenReturn(List.of());
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        RestaurantResponseDTO response = restaurantService.activateRestaurant(1L);

        assertEquals(RestaurantStatus.ACTIVE, response.getStatus());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testSuspendRestaurant() {
        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of());
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        ))
                .thenReturn(List.of());
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        RestaurantResponseDTO response = restaurantService.suspendRestaurant(1L);

        assertEquals(RestaurantStatus.SUSPENDED, response.getStatus());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testDeleteRestaurant() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        restaurantService.delete(1L);

        verify(restaurantRepository, times(1)).delete(restaurant);
    }

    @Test
    void testDeleteRestaurantNotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> restaurantService.delete(1L));

        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void testGetById() {
        qr_ordering_system.model.Order completedOrder = new qr_ordering_system.model.Order();
        completedOrder.setTotalAmount(42.5);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.findByTenantIdAndStatusIn(1L, List.of(
                OrderStatus.PENDING,
                OrderStatus.PREPARING,
                OrderStatus.READY
        ))).thenReturn(List.of(new qr_ordering_system.model.Order(), new qr_ordering_system.model.Order()));
        when(orderRepository.findOrdersForRestaurant(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(List.of(OrderStatus.COMPLETED)),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(List.of(completedOrder));

        RestaurantResponseDTO response = restaurantService.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getActiveOrderCount());
        assertEquals(42.5, response.getTodayRevenue());
        assertEquals("LKR", response.getCurrencyCode());
    }
}
