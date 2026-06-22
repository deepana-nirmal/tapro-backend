package qr_ordering_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.SuperAdminInvitationResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.InvitationService;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/invitations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminInvitationController {

    private final InvitationService invitationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminInvitationResponseDTO>>> listInvitations() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Invitations retrieved successfully",
                invitationService.listInvitations()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendInvitation(
            @Valid @RequestBody InvitationRequest request,
            Authentication authentication
    ) {
        invitationService.sendInvitation(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Invitation sent successfully",
                null
        ));
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<Void>> deleteInvitation(@PathVariable Long invitationId) {
        invitationService.deleteInvitation(invitationId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Invitation deleted successfully",
                null
        ));
    }
}
