package qr_ordering_system.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.ApiResponse;
import qr_ordering_system.dto.CategoryResponseDTO;
import qr_ordering_system.dto.OwnerAnalyticsResponse;
import qr_ordering_system.dto.OwnerStaffUserResponse;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.dto.RestaurantResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.service.CategoryService;
import qr_ordering_system.service.OwnerService;
import qr_ordering_system.service.RestaurantService;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    @GetMapping("/orders/active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OrderResponseDTO>> getActiveOrders(Authentication authentication) {
        return ResponseEntity.ok(ownerService.getActiveOrders(authentication.getName()));
    }

    @GetMapping("/orders/past")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OrderResponseDTO>> getPastOrders(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String tableNumber,
            Authentication authentication) {
        return ResponseEntity.ok(ownerService.getPastOrders(
                authentication.getName(),
                from,
                to,
                status,
                tableNumber
        ));
    }

    // ✅ GET ALL ORDERS
    @GetMapping("/orders/{restaurantId}")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @PathVariable Long restaurantId,
            Authentication authentication) {
        return ResponseEntity.ok(ownerService.getAllOrders(restaurantId, authentication.getName()));
    }

    // ✅ GET BY STATUS
    @GetMapping("/orders/{restaurantId}/status")
    public ResponseEntity<List<OrderResponseDTO>> getByStatus(
            @PathVariable Long restaurantId,
            @RequestParam OrderStatus status,
            Authentication authentication) {

        return ResponseEntity.ok(ownerService.getOrdersByStatus(restaurantId, status, authentication.getName()));
    }

    // ✅ GET SINGLE ORDER
    @GetMapping("/orders/single/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return ResponseEntity.ok(ownerService.getOrder(orderId, authentication.getName()));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Order status updated successfully",
                        ownerService.updateOrderStatus(orderId, status, authentication.getName())
                )
        );
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> getAnalytics(Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Owner analytics retrieved successfully",
                        ownerService.getAnalytics(authentication.getName())
                )
        );
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> getManagedStaff(Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Staff users retrieved successfully",
                        ownerService.getManagedStaff(authentication.getName())
                )
        );
    }

    @PutMapping("/staff/{userId}/enable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> enableStaff(
            @PathVariable Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "User enabled successfully",
                        ownerService.updateStaffStatus(userId, true, authentication.getName())
                )
        );
    }

    @PutMapping("/staff/{userId}/disable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> disableStaff(
            @PathVariable Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "User disabled successfully",
                        ownerService.updateStaffStatus(userId, false, authentication.getName())
                )
        );
    }

    @PostMapping("/staff/{userId}/reset-password")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse> resetStaffPassword(
            @PathVariable Long userId,
            Authentication authentication) {
        ownerService.resetStaffPassword(userId, authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Password reset email sent successfully",
                        null
                )
        );
    }

    @PostMapping(value = "/restaurant/logo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<qr_ordering_system.payload.ApiResponse<RestaurantResponseDTO>> uploadRestaurantLogo(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(new qr_ordering_system.payload.ApiResponse<>(
                true,
                "Restaurant logo uploaded successfully",
                restaurantService.uploadLogoAsOwner(authentication.getName(), file)
        ));
    }

    @PostMapping(value = "/categories/{categoryId}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<qr_ordering_system.payload.ApiResponse<CategoryResponseDTO>> uploadCategoryImage(
            @PathVariable Long categoryId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(new qr_ordering_system.payload.ApiResponse<>(
                true,
                "Category image uploaded successfully",
                categoryService.uploadImage(categoryId, file, authentication.getName())
        ));
    }
}
