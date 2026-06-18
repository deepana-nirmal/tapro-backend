package qr_ordering_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import qr_ordering_system.dto.SuperAdminUserRequestDTO;
import qr_ordering_system.dto.SuperAdminUserResponseDTO;
import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.OrderRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.service.impl.AdminServiceImpl;

class AdminServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Restaurant restaurant;
    private User ownerUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("KFC Colombo");
        restaurant.setStatus(RestaurantStatus.ACTIVE);

        ownerUser = new User();
        ownerUser.setId(11L);
        ownerUser.setName("Owner");
        ownerUser.setEmail("owner@kfc.com");
        ownerUser.setRole(Role.OWNER);
        ownerUser.setRestaurant(restaurant);
        ownerUser.setEnabled(true);
    }

    @Test
    void testGetAllRestaurants() {
        when(restaurantRepository.findAll())
                .thenReturn(List.of(restaurant));

        List<Restaurant> result = adminService.getAllRestaurants();

        assertEquals(1, result.size());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testSuspendRestaurant() {
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        adminService.suspendRestaurant(1L);

        assertEquals(RestaurantStatus.SUSPENDED, restaurant.getStatus());
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void testActivateRestaurant() {
        restaurant.setStatus(RestaurantStatus.SUSPENDED);
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        adminService.activateRestaurant(1L);

        assertEquals(RestaurantStatus.ACTIVE, restaurant.getStatus());
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void testDeleteRestaurant() {
        doNothing().when(restaurantRepository).deleteById(1L);

        adminService.deleteRestaurant(1L);

        verify(restaurantRepository).deleteById(1L);
    }

    @Test
    void testSuspendRestaurantNotFound() {
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.suspendRestaurant(1L));

        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void testGetUsersByRestaurant() {
        User staffUser = new User();
        staffUser.setId(12L);
        staffUser.setName("Staff");
        staffUser.setEmail("staff@kfc.com");
        staffUser.setRole(Role.STAFF);
        staffUser.setRestaurant(restaurant);
        staffUser.setEnabled(true);

        User superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setName("Platform Admin");
        superAdminUser.setEmail("admin@tapro.com");
        superAdminUser.setRole(Role.SUPER_ADMIN);
        superAdminUser.setEnabled(true);

        when(restaurantRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(restaurant));
        when(userRepository.findAllWithRestaurant())
                .thenReturn(List.of(ownerUser, staffUser, superAdminUser));

        List<UsersByRestaurantResponseDTO> response = adminService.getUsersByRestaurant();

        assertEquals(2, response.size());
        assertEquals("KFC Colombo", response.get(0).getRestaurantName());
        assertEquals(1, response.get(0).getOwners().size());
        assertEquals(1, response.get(0).getStaff().size());
        assertEquals("Unassigned / Platform Users", response.get(1).getRestaurantName());
        assertEquals(1, response.get(1).getSuperAdmins().size());
    }

    @Test
    void testCreateUser() {
        SuperAdminUserRequestDTO request = new SuperAdminUserRequestDTO();
        request.setName("Kitchen User");
        request.setEmail("kitchen@kfc.com");
        request.setPassword("secret123");
        request.setRole(Role.KITCHEN);
        request.setRestaurantId(1L);
        request.setEnabled(true);

        when(userRepository.existsByEmail("kitchen@kfc.com")).thenReturn(false);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        SuperAdminUserResponseDTO response = adminService.createUser(request);

        assertEquals(20L, response.getId());
        assertEquals(Role.KITCHEN, response.getRole());
        assertEquals(1L, response.getRestaurantId());
    }

    @Test
    void testUpdateUser() {
        SuperAdminUserRequestDTO request = new SuperAdminUserRequestDTO();
        request.setName("Updated Owner");
        request.setEmail("updated@kfc.com");
        request.setRole(Role.OWNER);
        request.setRestaurantId(1L);
        request.setEnabled(false);

        when(userRepository.findById(11L)).thenReturn(Optional.of(ownerUser));
        when(userRepository.existsByEmailAndIdNot("updated@kfc.com", 11L)).thenReturn(false);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuperAdminUserResponseDTO response = adminService.updateUser(11L, request);

        assertEquals("Updated Owner", response.getName());
        assertFalse(response.isEnabled());
    }

    @Test
    void testEnableUser() {
        ownerUser.setEnabled(false);
        when(userRepository.findById(11L)).thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuperAdminUserResponseDTO response = adminService.enableUser(11L);

        assertTrue(response.isEnabled());
    }

    @Test
    void testDisableUser() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(ownerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuperAdminUserResponseDTO response = adminService.disableUser(11L);

        assertFalse(response.isEnabled());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(11L)).thenReturn(Optional.of(ownerUser));

        adminService.deleteUser(11L);

        verify(userRepository).delete(ownerUser);
    }

    @Test
    void testPreventUserWithoutRestaurant() {
        SuperAdminUserRequestDTO request = new SuperAdminUserRequestDTO();
        request.setName("Staff User");
        request.setEmail("staff@kfc.com");
        request.setPassword("secret123");
        request.setRole(Role.STAFF);
        request.setEnabled(true);

        when(userRepository.existsByEmail("staff@kfc.com")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> adminService.createUser(request));

        assertEquals("Restaurant is required for OWNER, STAFF, and KITCHEN users", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testPreventManagingSuperAdmin() {
        User superAdminUser = new User();
        superAdminUser.setId(99L);
        superAdminUser.setRole(Role.SUPER_ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(superAdminUser));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> adminService.deleteUser(99L));

        assertEquals("This user cannot be managed from the super admin users screen", exception.getMessage());
    }
}
