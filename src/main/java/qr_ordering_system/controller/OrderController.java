package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import qr_ordering_system.dto.OrderRequestDTO;
import qr_ordering_system.dto.OrderResponseDTO;
import qr_ordering_system.model.OrderStatus;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@RequestBody OrderRequestDTO dto) {
        log.info("POST /api/orders restaurantId={} tableNumber={}", dto.getRestaurantId(), dto.getTableNumber());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order created", orderService.createOrder(dto))
        );
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getPublicById(
            @PathVariable Long id,
            @RequestParam(required = false) Long restaurantId) {
        log.info("GET /api/orders/public/{} restaurantId={}", id, restaurantId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order fetched", restaurantId != null
                        ? orderService.getPublicOrderById(restaurantId, id)
                        : orderService.getPublicOrderById(id))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getById(@PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order fetched", orderService.getOrderById(id))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getAll() {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders fetched", orderService.getAllOrders())
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getByStatus(@PathVariable OrderStatus status) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders fetched", orderService.getOrdersByStatus(status))
        );
    }

    @GetMapping("/table/{tableNumber}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getByTable(@PathVariable String tableNumber) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders fetched", orderService.getOrdersByTable(tableNumber))
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Status updated", orderService.updateOrderStatus(id, newStatus))
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {

        orderService.cancelOrder(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order cancelled", null)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order deleted", null)
        );
    }
}
