package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.KitchenService;

@RestController
@RequestMapping("/api/kitchen/orders")
@RequiredArgsConstructor
@CrossOrigin
public class KitchenController {

    private final KitchenService kitchenService;

    // =========================
    // GET ALL KITCHEN ORDERS (SAAS)
    // =========================
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getKitchenOrders() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Kitchen orders retrieved successfully",
                        kitchenService.getKitchenOrders()
                )
        );
    }

    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getPastOrders() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Past orders retrieved successfully",
                        kitchenService.getPastOrders()
                )
        );
    }

    // =========================
    // FILTER BY STATUS
    // =========================
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getByStatus(
            @PathVariable OrderStatus status) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Orders retrieved successfully",
                        kitchenService.getOrdersByStatus(status)
                )
        );
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Order status updated successfully",
                        kitchenService.updateOrderStatus(orderId, status)
                )
        );
    }
}
