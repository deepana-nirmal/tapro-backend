package qr_ordering_system.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.RegisterRequestDTO;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.RestaurantStatus;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.security.JwtUtil;
import qr_ordering_system.service.RestaurantAccessGuard;
import qr_ordering_system.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RestaurantAccessGuard restaurantAccessGuard;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldSaveUserSuccessfully() {

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@gmail.com");
        request.setPassword("123456");
        request.setRole("CUSTOMER");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("encodedPassword");

        String result = authService.register(request);

        assertEquals("User registered successfully", result);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@gmail.com");
        request.setPassword("123456");
        request.setRole("CUSTOMER");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.register(request));

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {

        User user = new User();
        user.setEmail("admin@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtUtil.generateToken("admin@gmail.com", "ADMIN"))
                .thenReturn("jwt-token");

        AuthResponse response =
                authService.login("admin@gmail.com", "password123");

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.login("unknown@gmail.com", "password"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {

        User user = new User();
        user.setEmail("admin@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.login("admin@gmail.com", "wrongPassword"));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void login_ShouldRejectSuspendedOwner() {
        User user = restaurantUser("owner@demo.com", Role.OWNER, RestaurantStatus.SUSPENDED);

        when(userRepository.findByEmail("owner@demo.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new BadRequestException(RestaurantAccessGuard.SUSPENDED_MESSAGE))
                .when(restaurantAccessGuard).ensureLoginAllowed(user);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login("owner@demo.com", "password123")
        );

        assertEquals(RestaurantAccessGuard.SUSPENDED_MESSAGE, exception.getMessage());
    }

    @Test
    void login_ShouldRejectSuspendedStaff() {
        User user = restaurantUser("staff@demo.com", Role.STAFF, RestaurantStatus.SUSPENDED);

        when(userRepository.findByEmail("staff@demo.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new BadRequestException(RestaurantAccessGuard.SUSPENDED_MESSAGE))
                .when(restaurantAccessGuard).ensureLoginAllowed(user);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login("staff@demo.com", "password123")
        );

        assertEquals(RestaurantAccessGuard.SUSPENDED_MESSAGE, exception.getMessage());
    }

    @Test
    void login_ShouldRejectSuspendedKitchenUser() {
        User user = restaurantUser("kitchen@demo.com", Role.KITCHEN, RestaurantStatus.SUSPENDED);

        when(userRepository.findByEmail("kitchen@demo.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new BadRequestException(RestaurantAccessGuard.SUSPENDED_MESSAGE))
                .when(restaurantAccessGuard).ensureLoginAllowed(user);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.login("kitchen@demo.com", "password123")
        );

        assertEquals(RestaurantAccessGuard.SUSPENDED_MESSAGE, exception.getMessage());
    }

    @Test
    void login_ShouldAllowSuperAdminWithoutRestaurant() {
        User user = new User();
        user.setEmail("super@demo.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.SUPER_ADMIN);

        when(userRepository.findByEmail("super@demo.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken("super@demo.com", "SUPER_ADMIN"))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login("super@demo.com", "password123");

        assertEquals("jwt-token", response.getToken());
        assertEquals("SUPER_ADMIN", response.getRole());
    }

    private User restaurantUser(String email, Role role, RestaurantStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setStatus(status);

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(role);
        user.setRestaurant(restaurant);
        return user;
    }
}
