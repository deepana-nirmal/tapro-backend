package qr_ordering_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import qr_ordering_system.model.Role;
import lombok.Data;

@Data
public class InvitationRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Restaurant is required")
    private Long restaurantId;

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }
}
