package qr_ordering_system.service;

import qr_ordering_system.dto.AcceptInvitationRequest;
import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.InvitationVerifyResponse;
import qr_ordering_system.dto.SuperAdminInvitationResponseDTO;

import java.util.List;

public interface InvitationService {

    void sendInvitation(InvitationRequest request, String inviterEmail);

    List<SuperAdminInvitationResponseDTO> listInvitations();

    void deleteInvitation(Long invitationId);

    InvitationVerifyResponse verifyInvitation(String token);

    AuthResponse acceptInvitation(AcceptInvitationRequest request);
}
