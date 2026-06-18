package qr_ordering_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import qr_ordering_system.dto.AcceptInvitationRequest;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.InvitationVerifyResponse;
import qr_ordering_system.model.Invitation;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.User;
import qr_ordering_system.repository.InvitationRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.security.JwtUtil;
import qr_ordering_system.service.impl.InvitationServiceImpl;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    private Invitation validInvitation;
    private User adminInviter;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");

        validInvitation = Invitation.builder()
                .id(1L)
                .email("owner@test.com")
                .role(Role.OWNER)
                .token("valid-token")
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        adminInviter = new User();
        adminInviter.setId(99L);
        adminInviter.setEmail("admin@test.com");
        adminInviter.setRole(Role.ADMIN);
        adminInviter.setRestaurant(restaurant);
    }

    @Test
    void sendInvitation_ShouldSendEmail_WhenValidRequest() {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("owner@test.com");
        request.setRole(Role.OWNER);
        request.setRestaurantId(1L);

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminInviter));
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.empty());

        when(invitationRepository.existsByEmailAndUsedFalseAndExpiresAtAfter(eq("owner@test.com"), any(LocalDateTime.class)))
                .thenReturn(false);

        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        invitationService.sendInvitation(request, "admin@test.com");

        verify(invitationRepository, times(1))
                .save(any(Invitation.class));

        verify(emailService, times(1))
                .sendEmail(
                        eq("owner@test.com"),
                        eq("Tapro Invitation"),
                        contains("OWNER")
                );
    }

    @Test
    void sendInvitation_ShouldThrowException_WhenRoleIsSuperAdmin() {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("admin@test.com");
        request.setRole(Role.SUPER_ADMIN);
        request.setRestaurantId(1L);

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminInviter));
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.sendInvitation(request, "admin@test.com")
        );

        assertEquals("Cannot invite this role", exception.getMessage());

        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendInvitation_ShouldThrowException_WhenUserAlreadyExists() {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("owner@test.com");
        request.setRole(Role.OWNER);
        request.setRestaurantId(1L);

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminInviter));
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.sendInvitation(request, "admin@test.com")
        );

        assertEquals("User already exists", exception.getMessage());

        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendInvitation_ShouldThrowException_WhenActiveInvitationExists() {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("owner@test.com");
        request.setRole(Role.OWNER);
        request.setRestaurantId(1L);

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminInviter));
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.empty());

        when(invitationRepository.existsByEmailAndUsedFalseAndExpiresAtAfter(eq("owner@test.com"), any(LocalDateTime.class)))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.sendInvitation(request, "admin@test.com")
        );

        assertEquals("Active invitation already exists", exception.getMessage());

        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendInvitation_ShouldThrowException_WhenRestaurantMissing() {
        InvitationRequest request = new InvitationRequest();
        request.setEmail("staff@test.com");
        request.setRole(Role.STAFF);
        request.setRestaurantId(999L);

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(adminInviter));
        when(restaurantRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.sendInvitation(request, "admin@test.com")
        );

        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void sendInvitation_ShouldThrowException_WhenOwnerTargetsDifferentRestaurant() {
        User ownerInviter = new User();
        ownerInviter.setEmail("owner@test.com");
        ownerInviter.setRole(Role.OWNER);
        ownerInviter.setRestaurant(restaurant);

        Restaurant otherRestaurant = new Restaurant();
        otherRestaurant.setId(2L);
        otherRestaurant.setName("Other Restaurant");

        InvitationRequest request = new InvitationRequest();
        request.setEmail("staff@test.com");
        request.setRole(Role.STAFF);
        request.setRestaurantId(2L);

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(ownerInviter));
        when(restaurantRepository.findById(2L))
                .thenReturn(Optional.of(otherRestaurant));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.sendInvitation(request, "owner@test.com")
        );

        assertEquals("Owners can invite users only to their assigned restaurant", exception.getMessage());
    }

    @Test
    void verifyInvitation_ShouldReturnValidResponse_WhenTokenIsValid() {
        when(invitationRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validInvitation));

        InvitationVerifyResponse response =
                invitationService.verifyInvitation("valid-token");

        assertTrue(response.isValid());
        assertEquals("owner@test.com", response.getEmail());
        assertEquals(Role.OWNER, response.getRole());
    }

    @Test
    void verifyInvitation_ShouldThrowException_WhenTokenInvalid() {
        when(invitationRepository.findByToken("bad-token"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.verifyInvitation("bad-token")
        );

        assertEquals("Invalid invitation token", exception.getMessage());
    }

    @Test
    void verifyInvitation_ShouldThrowException_WhenInvitationUsed() {
        validInvitation.setUsed(true);

        when(invitationRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validInvitation));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.verifyInvitation("valid-token")
        );

        assertEquals("Invitation already used", exception.getMessage());
    }

    @Test
    void verifyInvitation_ShouldThrowException_WhenInvitationExpired() {
        validInvitation.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(invitationRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validInvitation));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.verifyInvitation("valid-token")
        );

        assertEquals("Invitation expired", exception.getMessage());
    }

    @Test
    void acceptInvitation_ShouldCreateUserAndReturnAuthResponse_WhenValid() {
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken("valid-token");
        request.setName("Owner User");
        request.setPassword("password123");

        when(invitationRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validInvitation));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtil.generateToken("owner@test.com", "OWNER"))
                .thenReturn("jwt-token");

        var response = invitationService.acceptInvitation(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("OWNER", response.getRole());

        verify(userRepository, times(1)).save(any(User.class));
        verify(invitationRepository, times(1)).save(validInvitation);

        assertTrue(validInvitation.isUsed());
    }

    @Test
    void acceptInvitation_ShouldThrowException_WhenUserAlreadyRegistered() {
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken("valid-token");
        request.setName("Owner User");
        request.setPassword("password123");

        when(invitationRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validInvitation));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> invitationService.acceptInvitation(request)
        );

        assertEquals("User already registered", exception.getMessage());

        verify(userRepository, never()).save(any());
    }
}
