package qr_ordering_system.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import qr_ordering_system.dto.AcceptInvitationRequest;
import qr_ordering_system.dto.ApiResponse;
import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.InvitationVerifyResponse;
import qr_ordering_system.service.InvitationService;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> sendInvitation(
            @Valid @RequestBody InvitationRequest request,
            Authentication authentication
    ) {
        invitationService.sendInvitation(request, authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse(true, "Invitation sent successfully", null)
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyInvitation(
            @RequestParam String token
    ) {
        InvitationVerifyResponse response = invitationService.verifyInvitation(token);

        return ResponseEntity.ok(
                new ApiResponse(true, "Invitation verified successfully", response)
        );
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request
    ) {
        AuthResponse response = invitationService.acceptInvitation(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Account created successfully", response)
        );
    }
}
