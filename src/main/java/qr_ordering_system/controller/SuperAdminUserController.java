package qr_ordering_system.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import qr_ordering_system.dto.SuperAdminUserRequestDTO;
import qr_ordering_system.dto.SuperAdminUserResponseDTO;
import qr_ordering_system.dto.UsersByRestaurantResponseDTO;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.AdminService;

@RestController
@RequestMapping("/api/super-admin/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminUserController {

    private final AdminService adminService;

    public SuperAdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminUserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Users retrieved successfully",
                adminService.getAllUsers()
        ));
    }

    @GetMapping("/by-restaurant")
    public ResponseEntity<ApiResponse<List<UsersByRestaurantResponseDTO>>> getUsersByRestaurant() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Users grouped by restaurant retrieved successfully",
                adminService.getUsersByRestaurant()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SuperAdminUserResponseDTO>> createUser(
            @Valid @RequestBody SuperAdminUserRequestDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User created successfully",
                adminService.createUser(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuperAdminUserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody SuperAdminUserRequestDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User updated successfully",
                adminService.updateUser(id, request)
        ));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<SuperAdminUserResponseDTO>> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User enabled successfully",
                adminService.enableUser(id)
        ));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<SuperAdminUserResponseDTO>> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User disabled successfully",
                adminService.disableUser(id)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User deleted successfully",
                null
        ));
    }
}
