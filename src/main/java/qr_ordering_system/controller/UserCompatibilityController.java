package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.AdminService;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserCompatibilityController {

    private final AdminService adminService;

    public UserCompatibilityController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/by-restaurant")
    public ResponseEntity<ApiResponse<List<UsersByRestaurantResponseDTO>>> getUsersByRestaurant() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Users grouped by restaurant retrieved successfully",
                adminService.getUsersByRestaurant()
        ));
    }
}
