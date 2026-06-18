package qr_ordering_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import qr_ordering_system.payload.ApiResponse;
import qr_ordering_system.dto.TableRequestDTO;
import qr_ordering_system.dto.TableResponseDTO;
import qr_ordering_system.service.TableService;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Validated
public class TableController {

    private final TableService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TableResponseDTO>> createTable(
            @Valid @RequestBody TableRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Table created successfully",
                        service.createTable(dto, authentication.getName())
                )
        );
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TableResponseDTO>>> getRestaurantTables(
            @PathVariable Long restaurantId,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Tables retrieved successfully",
                        service.getRestaurantTables(restaurantId, authentication.getName())
                )
        );
    }

    @PutMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TableResponseDTO>> updateTable(
            @PathVariable Long tableId,
            @Valid @RequestBody TableRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Table updated successfully",
                        service.updateTable(tableId, dto, authentication.getName())
                )
        );
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(
            @PathVariable Long tableId,
            Authentication authentication) {
        service.deleteTable(tableId, authentication.getName());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Table deleted successfully",
                        null
                )
        );
    }

    @GetMapping("/public/restaurant/{restaurantId}/table/{tableId}")
    public ResponseEntity<ApiResponse<TableResponseDTO>> getPublicTable(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Table retrieved successfully",
                        service.getPublicTable(restaurantId, tableId)
                )
        );
    }
}
