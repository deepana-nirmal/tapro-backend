package qr_ordering_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import qr_ordering_system.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerStaffUserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;
    private Long restaurantId;
    private String restaurantName;
}
