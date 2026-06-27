package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.service.AdminService;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.RestaurantService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final RestaurantService restaurantService;

    public AdminController(AdminService adminService, RestaurantService restaurantService) {
        this.adminService = adminService;
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDTO>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Restaurants retrieved successfully",
                        restaurantService.getAllRestaurants()
                )
        );
    }

    @PutMapping("/restaurants/{id}/suspend")
    public String suspend(@PathVariable Long id) {
        adminService.suspendRestaurant(id);
        return "Restaurant suspended";
    }

    @PutMapping("/restaurants/{id}/activate")
    public String activate(@PathVariable Long id) {
        adminService.activateRestaurant(id);
        return "Restaurant activated";
    }

    @DeleteMapping("/restaurants/{id}")
    public String delete(@PathVariable Long id) {
        adminService.deleteRestaurant(id);
        return "Restaurant deleted";
    }
}
