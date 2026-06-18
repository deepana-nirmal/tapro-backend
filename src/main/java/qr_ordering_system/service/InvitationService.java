package qr_ordering_system.service;

import qr_ordering_system.dto.AcceptInvitationRequest;
import qr_ordering_system.dto.AuthResponse;
import qr_ordering_system.dto.InvitationRequest;
import qr_ordering_system.dto.InvitationVerifyResponse;

public interface InvitationService {

    void sendInvitation(InvitationRequest request, String inviterEmail);

    InvitationVerifyResponse verifyInvitation(String token);

    AuthResponse acceptInvitation(AcceptInvitationRequest request);
}
