package qr_ordering_system.dto;

import qr_ordering_system.model.Role;

public class SuperAdminUserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long restaurantId;
    private String restaurantName;
    private boolean enabled;

    public SuperAdminUserResponseDTO() {
    }

    public SuperAdminUserResponseDTO(
            Long id,
            String name,
            String email,
            Role role,
            Long restaurantId,
            String restaurantName,
            boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
