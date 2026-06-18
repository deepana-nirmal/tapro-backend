package qr_ordering_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Data;

@Data
public class AcceptInvitationRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "Password is required")
    private String password;

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}