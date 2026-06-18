package qr_ordering_system.service.impl;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.AcceptInvitationRequest;
import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.InvitationVerifyResponse;
import qr_ordering_system.model.Invitation;
import qr_ordering_system.model.Role;
import qr_ordering_system.model.Restaurant;
import qr_ordering_system.model.User;
import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;
import qr_ordering_system.repository.InvitationRepository;
import qr_ordering_system.repository.RestaurantRepository;
import qr_ordering_system.repository.UserRepository;
import qr_ordering_system.security.JwtUtil;
import qr_ordering_system.service.EmailService;
import qr_ordering_system.service.InvitationService;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendInvitation(InvitationRequest request, String inviterEmail) {
        User inviter = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        Restaurant assignedRestaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        validateInvitationRequest(inviter, request.getRole(), assignedRestaurant);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("User already exists");
        }

        if (invitationRepository.existsByEmailAndUsedFalseAndExpiresAtAfter(request.getEmail(), LocalDateTime.now())) {
            throw new BadRequestException("Active invitation already exists");
        }

        String token = UUID.randomUUID().toString();

        Invitation invitation = new Invitation();
        invitation.setEmail(request.getEmail());
        invitation.setRole(request.getRole());
        invitation.setToken(token);
        invitation.setUsed(false);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setRestaurant(assignedRestaurant);

        invitationRepository.save(invitation);

        String inviteLink =
                frontendUrl + "/accept-invite?token=" + token;

        String subject = "Tapro Invitation";

        String body =
                "Hello,\n\n" +
                        "You have been invited to join Tapro as " +
                        request.getRole().name() +
                        ".\n\n" +
                        (assignedRestaurant != null
                                ? "Assigned restaurant: " + assignedRestaurant.getName() + "\n\n"
                                : "") +
                        "Please click the link below to verify your email and activate your account:\n\n" +
                        inviteLink +
                        "\n\n" +
                        "This invitation will expire in 24 hours.\n\n" +
                        "If you were not expecting this invitation, you may safely ignore this email.\n\n" +
                        "Tapro Team";

        emailService.sendEmail(
                request.getEmail(),
                subject,
                body
        );
    }

    @Override
    public InvitationVerifyResponse verifyInvitation(String token) {

        Invitation invitation = getValidInvitation(token);

        return new InvitationVerifyResponse(
                invitation.getEmail(),
                invitation.getRole(),
                true
        );
    }

    @Override
    public AuthResponse acceptInvitation(
            AcceptInvitationRequest request) {

        Invitation invitation =
                getValidInvitation(request.getToken());

        if (userRepository.findByEmail(
                invitation.getEmail()).isPresent()) {

            throw new BadRequestException(
                    "User already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(invitation.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        user.setRole(invitation.getRole());
        user.setRestaurant(invitation.getRestaurant());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        invitation.setUsed(true);

        invitationRepository.save(invitation);

        String jwt = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(
                jwt,
                savedUser.getRole().name(),
                savedUser.getRestaurant() != null ? savedUser.getRestaurant().getId() : null
        );
    }

    private Invitation getValidInvitation(
            String token) {

        Invitation invitation =
                invitationRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid invitation token"));

        if (invitation.isUsed()) {
            throw new BadRequestException(
                    "Invitation already used");
        }

        if (invitation.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Invitation expired");
        }

        return invitation;
    }

    private void validateInvitationRequest(User inviter, Role invitedRole, Restaurant restaurant) {
        if (invitedRole == Role.SUPER_ADMIN || invitedRole == Role.CUSTOMER) {
            throw new BadRequestException("Cannot invite this role");
        }

        if (inviter.getRole() == Role.OWNER) {
            if (!EnumSet.of(Role.STAFF, Role.KITCHEN).contains(invitedRole)) {
                throw new BadRequestException("Owners can invite only STAFF or KITCHEN users");
            }

            if (inviter.getRestaurant() == null) {
                throw new BadRequestException("Owner must be assigned to a restaurant before inviting staff");
            }

            if (!inviter.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("Owners can invite users only to their assigned restaurant");
            }

            return;
        }

        if (EnumSet.of(Role.ADMIN, Role.SUPER_ADMIN).contains(inviter.getRole())) {
            if (!EnumSet.of(Role.OWNER, Role.STAFF, Role.KITCHEN).contains(invitedRole)) {
                throw new BadRequestException("Only OWNER, STAFF, or KITCHEN invitations are allowed");
            }

            return;
        }

        throw new BadRequestException("This account cannot send invitations");
    }
}
