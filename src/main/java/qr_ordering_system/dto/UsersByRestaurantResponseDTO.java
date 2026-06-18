package qr_ordering_system.dto;

import java.util.ArrayList;
import java.util.List;

public class UsersByRestaurantResponseDTO {

    private Long restaurantId;
    private String restaurantName;
    private List<SuperAdminUserResponseDTO> owners = new ArrayList<>();
    private List<SuperAdminUserResponseDTO> staff = new ArrayList<>();
    private List<SuperAdminUserResponseDTO> kitchen = new ArrayList<>();
    private List<SuperAdminUserResponseDTO> superAdmins = new ArrayList<>();
    private List<SuperAdminUserResponseDTO> unassigned = new ArrayList<>();

    public UsersByRestaurantResponseDTO() {
    }

    public UsersByRestaurantResponseDTO(Long restaurantId, String restaurantName) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
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

    public List<SuperAdminUserResponseDTO> getOwners() {
        return owners;
    }

    public void setOwners(List<SuperAdminUserResponseDTO> owners) {
        this.owners = owners;
    }

    public List<SuperAdminUserResponseDTO> getStaff() {
        return staff;
    }

    public void setStaff(List<SuperAdminUserResponseDTO> staff) {
        this.staff = staff;
    }

    public List<SuperAdminUserResponseDTO> getKitchen() {
        return kitchen;
    }

    public void setKitchen(List<SuperAdminUserResponseDTO> kitchen) {
        this.kitchen = kitchen;
    }

    public List<SuperAdminUserResponseDTO> getSuperAdmins() {
        return superAdmins;
    }

    public void setSuperAdmins(List<SuperAdminUserResponseDTO> superAdmins) {
        this.superAdmins = superAdmins;
    }

    public List<SuperAdminUserResponseDTO> getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(List<SuperAdminUserResponseDTO> unassigned) {
        this.unassigned = unassigned;
    }
}
