package qr_ordering_system.dto;

import qr_ordering_system.model.Role;

public class InvitationVerifyResponse {

    private String email;
    private Role role;
    private boolean valid;

    public InvitationVerifyResponse(String email, Role role, boolean valid) {
        this.email = email;
        this.role = role;
        this.valid = valid;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isValid() {
        return valid;
    }
}