package qr_ordering_system.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.dto.MenuItemResponseDTO;
import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.AdminService;
import qr_ordering_system.service.CategoryService;

@RestController
@RequestMapping("/api/super-admin/restaurants/{restaurantId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminRestaurantController {

    private final AdminService adminService;
    private final CategoryService categoryService;

    @GetMapping("/orders/active")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getActiveOrders(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Active orders retrieved successfully",
                adminService.getRestaurantActiveOrders(restaurantId)
        ));
    }

    @GetMapping("/orders/past")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getPastOrders(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String tableNumber) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Past orders retrieved successfully",
                adminService.getRestaurantPastOrders(restaurantId, from, to, status, tableNumber)
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<OwnerStaffUserResponse>>> getUsers(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant users retrieved successfully",
                adminService.getRestaurantUsers(restaurantId)
        ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<OwnerAnalyticsResponse>> getAnalytics(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant analytics retrieved successfully",
                adminService.getRestaurantAnalytics(restaurantId)
        ));
    }

    @GetMapping("/menu-items")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getMenuItems(
            @PathVariable Long restaurantId,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant menu items retrieved successfully",
                adminService.getRestaurantMenuItems(restaurantId, authentication.getName())
        ));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategories(
            @PathVariable Long restaurantId,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant categories retrieved successfully",
                adminService.getRestaurantCategories(restaurantId, authentication.getName())
        ));
    }

    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<List<TableResponseDTO>>> getTables(
            @PathVariable Long restaurantId,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Restaurant tables retrieved successfully",
                adminService.getRestaurantTables(restaurantId, authentication.getName())
        ));
    }

    @PostMapping(value = "/categories/{categoryId}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> uploadCategoryImage(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Category image uploaded successfully",
                categoryService.uploadImageForRestaurant(restaurantId, categoryId, file)
        ));
    }
}
